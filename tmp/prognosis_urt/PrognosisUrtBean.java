package ru.sanors.energy_portal.sc.prognosis_urt;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sanors.energy_portal.constants.ErrorCode;
import ru.sanors.energy_portal.core.constants.DateFormat;
import ru.sanors.energy_portal.core.statuses.OperationStatus;
import ru.sanors.energy_portal.reports.loader.AbstractLoaderBean;
import ru.sanors.energy_portal.sc.util.EPDateFormat;
import ru.sanors.energy_portal.sc.web_interface.CommonRestService;
import ru.sanors.energy_portal.sc.web_portal.prognosis_urt.*;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

/**
 * ������� ����� ������� �� ����� ���� - ����������� � �������� ��������� "set_specific_additional_loading"
 */

@Stateless(name = PrognosisUrt.JNDI_NAME)
@Remote(PrognosisUrt.class)
@TransactionManagement(value = TransactionManagementType.BEAN)
public class PrognosisUrtBean extends AbstractLoaderBean implements PrognosisUrt {
    private static final Logger LOG = LoggerFactory.getLogger(PrognosisUrt.class);

//*******�������
    @Override
    public List<PrognosisUrtRecord> getPrognosisUrtStation(UUID uuid, DateTime dateTime,Integer generatorType) {
        return getPrognosisUrtRecordList(getStationQueryHead() + getBasicQuery(generatorType) + getStationQueryFoot(), dateTime);
    }

//*******����������
    @Override
    public List<PrognosisUrtGetVariantsRecord> getPrognosisUrtGenerators(UUID uuid, DateTime bdt, String hour) {
        int hr = Integer.parseInt(hour);
        List<PrognosisUrtGetVariantsRecord> responseRecords = new ArrayList<>();
        Timestamp date = new Timestamp(bdt.plusHours( hr ).toDateTime().getMillis());
        List<String> urtTypes = getVariants(bdt.withHourOfDay(hr));

        String queryString = getQueryString(urtTypes);

        Query q = getEntityManager().createNativeQuery(queryString);
        q = q.setParameter("bdt", date);
        List<Object[]> result = q.getResultList();

        for (Object[] fa : result) {
            PrognosisUrtGetVariantsRecord record = new PrognosisUrtGetVariantsRecord();

            record.orderer = getCellData(fa[0]);
            record.valueName = getCellData(fa[1]);
            record.measureUnit = getCellData(fa[2]);
            record.tgGroup = getCellData(fa[3]);
            record.values = new ArrayList<>();
            for (int i = 4; i < fa.length; i++ ) {
                record.values.add( getCellData(fa[i]) );
            }
            responseRecords.add(record);
        }

        return responseRecords;
    }

    @Override
    public OperationStatus setPrognosisUrtGenerators(UUID uuid, CommonRestService commonRestService, PrognosisUrtSetRecord record) {

        rightCheck("edit_urt_generator", findWebUser(commonRestService.getUserID()).getLogin());

        setData(record);

        try {
            return recalcUrtVariantHour(record.dt, String.valueOf(record.hour), record.variant);
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }
    }

    @Override
    public OperationStatus addVariant(UUID uuid, PrognosisUrtVariantRecord record) {

        DateTime dateTime = getDateTime(record.dt, record.hour);
        String urtTypeFrom = getCurrentVariantUrtType(record.variantNumber);
        String urtTypeTo = getNextVariantUrtType(record.variantNumber);
        String dt = " Cast('" + DateFormat.getDateFormatHrMmSs().format(dateTime.toDate()) + "' AS DATETIME)";
        String dt_param = " Cast('" + DateFormat.getDateFormatHrMmSs().format(dateTime.withTime(0,0,0,0).toDate()) + "' AS DATETIME)";

        String deleteSQL = "DECLARE @dt DATETIME = " + dt + "\n\n"
                + "delete FROM [dbo].[urt_generator] where dt = @dt  and [urt_type] = '" + urtTypeTo + "'\n\n"
                + "delete from [dbo].[urt_parameters] where dt = @dt and [urt_type] = '" + urtTypeTo + "'\n\n";

        String insertSQL = "DECLARE @dt DATETIME = " + dt + "\n" +
                "DECLARE @dt_param DATETIME = " + dt_param + "\n" +
                copyVariantIntoUrtGenerator(urtTypeTo, urtTypeFrom) +
                copyVariantIntoUrtParameters(urtTypeTo, urtTypeFrom);
        try {
            int code = execute(insertSQL, deleteSQL);
            String hr = String.valueOf(record.hour);
            return recalcUrtVariantHour(record.dt, hr, urtTypeTo);
        } catch (Exception e) {
            LOG.error(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }
    }

    @Override
    public OperationStatus removeVariant(UUID uuid, CommonRestService commonRestService, PrognosisUrtVariantRecord record) {

        String hr = String.valueOf(record.hour);
        String dt = " Cast('" + record.dt + " " + hr + ":00:00' AS DATETIME)";
        String urtType = getCurrentVariantUrtType(record.variantNumber);

        String deleteSql = "DECLARE @dt DATETIME = " + dt + "\n" +
                "delete FROM [dbo].[urt_calculation]\n" +
                "where dt = @dt  and [urt_type] = '" + urtType + "'\n\n" +
                "delete FROM [dbo].[urt_generator]\n" +
                "where dt = @dt  and [urt_type] = '" + urtType + "'\n\n" +
                "delete FROM [dbo].[urt_parameters]\n" +
                "where dt = @dt  and [urt_type] = '" + urtType + "'";
        try {
            int code = execute(deleteSql);
            return OperationStatus.OK;
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }

    }


    //*******���������
    @Override
    public List<PrognosisUrtParametersRecord> getPrognosisUrtParameters(UUID uuid, DateTime bdt, DateTime edt,Integer calculateType) {
        String urtType = (calculateType==0) ? "'prognosis'" : (calculateType==1) ? "'min_max'":"";
        List<PrognosisUrtParametersRecord> responseRecords = new ArrayList<PrognosisUrtParametersRecord>(0);

        LocalDateTime beginDt = bdt.withTime(0, 0, 0, 0).toLocalDateTime();
        LocalDateTime endDt = edt.withTime(23, 0, 0, 0).toLocalDateTime();

        String queryString = "--DECLARE @bdt DATETIME = Cast('1.01.2014 00:00:00' AS DATETIME)\n" +
                "--DECLARE @edt DATETIME = Cast('31.12.2014 23:00:00' AS DATETIME)\n" +
                "\n" +
                "DECLARE @bdt DATETIME = :bdt\n" +
                "DECLARE @edt DATETIME = :edt\n" +
                "DECLARE @urt_type varchar(max) = "+urtType+"\n"+
                "\n" +
                "select CalendarHR.dt\n" +
                "       ,datepart(hour,CalendarHR.dt)+1 as hour\n" +
                "       ,[������ ����������]\n" +
                "       ,[���� b��]\n" +
                "       ,[��� �����]\n" +
                "       ,[���1]\n" +
                "       ,[���2]\n" +
                "       ,[���� �� ���]\n" +
                "       ,[���� �� ���]\n" +
                "       ,[��� 100]\n" +
                "       ,[��� 21]\n" +
                "       ,[��� 13]\n" +
                "       ,[������������]\n" +
                "       ,[���� ��������]\n" +
                "       ,[� �� �� ��]\n" +
                "from CalendarHR\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[������ ����������]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[������ ����������]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "        where [������ ����������] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s2\n" +
                "on ((CalendarHR.dt >=s2.dt and CalendarHR.dt<s2.lead_dt) or (CalendarHR.dt >=s2.dt and s2.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[���� b��]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[���� b��]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "                  where [���� b��] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s3\n" +
                "on ((CalendarHR.dt >=s3.dt and CalendarHR.dt<s3.lead_dt) or (CalendarHR.dt >=s3.dt and s3.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[��� �����]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[��� �����]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "                  where [��� �����] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s4\n" +
                "on ((CalendarHR.dt >=s4.dt and CalendarHR.dt<s4.lead_dt) or (CalendarHR.dt >=s4.dt and s4.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[���1]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[���1]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [���1] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s5\n" +
                "on ((CalendarHR.dt >=s5.dt and CalendarHR.dt<s5.lead_dt) or (CalendarHR.dt >=s5.dt and s5.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[���2]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[���2]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n" +
                "                where [���2] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s6\n" +
                "on ((CalendarHR.dt >=s6.dt and CalendarHR.dt<s6.lead_dt) or (CalendarHR.dt >=s6.dt and s6.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[���� �� ���]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[���� �� ���]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [���� �� ���] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s7\n" +
                "on ((CalendarHR.dt >=s7.dt and CalendarHR.dt<s7.lead_dt) or (CalendarHR.dt >=s7.dt and s7.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[���� �� ���]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[���� �� ���]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [���� �� ���] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s8\n" +
                "on ((CalendarHR.dt >=s8.dt and CalendarHR.dt<s8.lead_dt) or (CalendarHR.dt >=s8.dt and s8.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[��� 100]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[��� 100]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [��� 100] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s9\n" +
                "on ((CalendarHR.dt >=s9.dt and CalendarHR.dt<s9.lead_dt) or (CalendarHR.dt >=s9.dt and s9.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[��� 21]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[��� 21]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [��� 21] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s10\n" +
                "on ((CalendarHR.dt >=s10.dt and CalendarHR.dt<s10.lead_dt) or (CalendarHR.dt >=s10.dt and s10.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                  ,[��� 13]\n" +
                "                  ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                select dt\n" +
                "                       ,[��� 13]\n" +
                "                       ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                from [urt_parameters]\n"+
                "                where [��� 13] is not null and [urt_type] = @urt_type\n" +
                "                )s1\n" +
                "           )s11\n" +
                "on ((CalendarHR.dt >=s11.dt and CalendarHR.dt<s11.lead_dt) or (CalendarHR.dt >=s11.dt and s11.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[������������]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[������������]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "        where [������������] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s12\n" +
                "on ((CalendarHR.dt >=s12.dt and CalendarHR.dt<s12.lead_dt) or (CalendarHR.dt >=s12.dt and s12.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[���� ��������]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[���� ��������]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "        where [���� ��������] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s13\n" +
                "on ((CalendarHR.dt >=s13.dt and CalendarHR.dt<s13.lead_dt) or (CalendarHR.dt >=s13.dt and s13.lead_dt is null))\n" +
                "left join (\n" +
                "           select dt\n" +
                "                    ,[� �� �� ��]\n" +
                "                    ,iif(lead_dt = cast('1.01.1900 00:00:00' as datetime),null,lead_dt) as lead_dt\n" +
                "           from(\n" +
                "                  select dt\n" +
                "                         ,[� �� �� ��]\n" +
                "                         ,lead(dt,1,0) over (order by dt) as lead_dt\n" +
                "                  from [urt_parameters]\n"+
                "        where [� �� �� ��] is not null and [urt_type] = @urt_type\n" +
                "                  )s1\n" +
                "           )s14\n" +
                "on ((CalendarHR.dt >=s14.dt and CalendarHR.dt<s14.lead_dt) or (CalendarHR.dt >=s14.dt and s14.lead_dt is null))\n" +
                "where CalendarHR.dt between @bdt and @edt\n" +
                "order by dt";


        Query q = getEntityManager().createNativeQuery(queryString);
        List<Object[]> result = q.setParameter("bdt", new Timestamp(beginDt.toDateTime().getMillis()))
                .setParameter("edt", new Timestamp(endDt.toDateTime().getMillis())).getResultList();

        for (Object[] fa : result) {
            PrognosisUrtParametersRecord record = new PrognosisUrtParametersRecord();
            record.dt = getCellData(fa[0], DateFormat.getDateFormat());
            record.hour = getCellData(fa[1]);
            record.condensateLosses = getCellData(fa[2]);
            record.coeffBte = getCellData(fa[3]);
            record.boilerEfficiency = getCellData(fa[4]);
            record.kTv1 = getCellData(fa[5]);
            record.kTv2 = getCellData(fa[6]);
            record.coeffSnEtl = getCellData(fa[7]);
            record.coeffSnEel = getCellData(fa[8]);
            record.rou100 = getCellData(fa[9]);
            record.rou21 = getCellData(fa[10]);
            record.rou13 = getCellData(fa[11]);
            record.caloricContent = getCellData(fa[12]);
            record.bUslKindling = getCellData(fa[13]);
            record.bTeNrPr = getCellData(fa[14]);
            responseRecords.add(record);
        }

        return responseRecords;
    }

    @Override
    public OperationStatus setPrognosisUrtParameters(UUID uuid, CommonRestService commonRestService, PrognosisUrtParametersRecord record) {
        String parametersTable = (record.calculateType==0) ? "parameters" : (record.calculateType==1) ? "min_max_parameters":"";

        rightCheck("edit_urt_"+parametersTable, findWebUser(commonRestService.getUserID()).getLogin());

        String sql = getCurrentParameterUpdateString(record);

        try {
            int code = execute(sql);
            return execStoredProcedureString(null, record);
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }

    }



    @Override
    public OperationStatus copyDayData(UUID uuid,CommonRestService commonRestService, DateTime fromDt, DateTime toDt) {
        String boilersTable = "boilers";
        String generatorTableName = "generator";

        String webUserLogin = findWebUser(commonRestService.getUserID()).getLogin();

        rightCheck("edit_urt_heat_supply", webUserLogin);
        rightCheck("edit_urt_"+boilersTable, webUserLogin);
        rightCheck("edit_urt_"+generatorTableName, webUserLogin);

        String declareDtString = "Declare @bdt_to datetime = cast('"+DateFormat.getDateFormatHrMmSs().format(toDt.withTime(0,0,0,0).toDate())+"' as datetime)\n" +
                "Declare @edt_to datetime = cast('"+DateFormat.getDateFormatHrMmSs().format(toDt.withTime(23,0,0,0).toDate())+"' as datetime)\n"+
                "Declare @bdt_from datetime = cast('"+DateFormat.getDateFormatHrMmSs().format(fromDt.withTime(0,0,0,0).toDate())+"' as datetime)\n" +
                "Declare @edt_from datetime = cast('"+DateFormat.getDateFormatHrMmSs().format(fromDt.withTime(23,0,0,0).toDate())+"' as datetime)\n";

        String copyHeatString = "Delete from [urt_heat_supply] where dt between @bdt_to and @edt_to\n"+
                "Insert into [urt_heat_supply](dt,[100 ���],[21 ���],[13 ���],[���])\n"+
                "Select calendarHR.dt, [100 ���],[21 ���],[13 ���],[���] \n" +
                "from calendarHR\n" +
                "left join(\n" +
                "          select dt, [100 ���],[21 ���],[13 ���],[���] from [urt_heat_supply] where dt between @bdt_from and @edt_from\n" +
                "         )t1 on datepart(hour,calendarHR.dt) = datepart(hour,t1.dt)\n" +
                "where calendarHR.dt between @bdt_to and @edt_to\n\n";

        String copyBoilersString = "Delete from [urt_"+boilersTable+"] where dt between @bdt_to and @edt_to and urt_type like 'prognosis%'\n" +
                "insert into [urt_"+boilersTable+"](dt,[���������� ������� ������],urt_type)\n" +
                "Select calendarHR.dt, [���������� ������� ������],urt_type\n" +
                "from calendarHR\n" +
                "left join(\n" +
                "          select dt, [���������� ������� ������],urt_type from [urt_"+boilersTable+"] where dt between @bdt_from and @edt_from and urt_type like 'prognosis%'\n" +
                "         )t1 on datepart(hour,calendarHR.dt) = datepart(hour,t1.dt)\n" +
                "where calendarHR.dt between @bdt_to and @edt_to\n\n";

        String copyGeneratorsString = "Delete from [urt_"+generatorTableName+"] where dt between @bdt_to and @edt_to and urt_type like 'prognosis%'\n" +
                "insert into [urt_"+generatorTableName+"](dt,[��������],[���������������� �����],[���������������� �����],[id ��],urt_type)\n" +
                "Select calendarHR.dt, [��������],[���������������� �����],[���������������� �����],[id ��],urt_type\n" +
                "from calendarHR\n" +
                "left join(\n" +
                "          select dt, [��������],[���������������� �����],[���������������� �����],[id ��],urt_type from [urt_"+generatorTableName+"] where dt between @bdt_from and @edt_from and urt_type like 'prognosis%'\n" +
                "         )t1 on datepart(hour,calendarHR.dt) = datepart(hour,t1.dt)\n" +
                "where calendarHR.dt between @bdt_to and @edt_to and [id ��] is not null\n\n";

        String totalCopyString = declareDtString+copyHeatString+copyGeneratorsString+copyBoilersString;

        try {
            int code = execute(totalCopyString);
            return recalcURT(toDt.withTime(0,0,0,0),toDt.withTime(23,0,0,0));
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }


    }

    @Override
    public boolean testCopyDayData(UUID uuid, DateTime fromDt, DateTime toDt) {
        LocalDateTime beginDt = fromDt.withTime(0, 0, 0, 0).toLocalDateTime();
        LocalDateTime endDt = fromDt.withTime(23, 0, 0, 0).toLocalDateTime();

        LocalDateTime beginDtSecond = toDt.withTime(0, 0, 0, 0).toLocalDateTime();
        LocalDateTime endDtSecond = toDt.withTime(23, 0, 0, 0).toLocalDateTime();

        String queryString =
                "DECLARE @bdt DATETIME = :bdt\n" +
                        "DECLARE @edt DATETIME = :edt\n" +
                        "DECLARE @bdt_second DATETIME = :bdt_second\n" +
                        "DECLARE @edt_second DATETIME = :edt_second\n" +

                        "\n" +
                        "--DECLARE @bdt DATETIME = Cast('15.05.2015 00:00:00' AS DATETIME)\n" +
                        "--DECLARE @edt DATETIME = Cast('15.05.2015 23:00:00' AS DATETIME)\n" +
                        "--DECLARE @bdt_second DATETIME = Cast('16.05.2015 00:00:00' AS DATETIME)\n" +
                        "--DECLARE @edt_second DATETIME = Cast('16.05.2015 23:00:00' AS DATETIME)\n" +
                        "\n" +
                        "\n" +
                        "SELECT avg(t1.[100 ���]) as [t1_100 ���]\n" +
                        "      ,avg(t2.[100 ���]) as [t2_100 ���]\n" +
                        "\n" +
                        "\t  ,avg(t1.[21 ���]) as [t1_21 ���]\n" +
                        "      ,avg(t2.[21 ���]) as [t2_21 ���]\n" +
                        "\n" +
                        "\t  ,avg(t1.[13 ���]) as [t1_13 ���]\n" +
                        "      ,avg(t2.[13 ���]) as [t2_13 ���]      \n" +
                        "\n" +
                        "\t  ,avg(t1.[���]) as [t1_���]                              \n" +
                        "      ,avg(t2.[���]) as [t2_���]      \n" +
                        "  FROM [dbo].[urt_heat_supply] t1 \n" +
                        "  join (SELECT * FROM [dbo].[urt_heat_supply] where dt between @bdt_second and @edt_second )t2 on datepart(hour,t1.dt) = datepart(hour,t2.dt)\n" +
                        "   where t1.dt between @bdt and @edt ";


        Query q = getEntityManager().createNativeQuery(queryString);
        List<Object[]> result = q.setParameter((String)"bdt", new Timestamp(beginDt.toDateTime().getMillis()))
                .setParameter((String)"edt", new Timestamp(endDt.toDateTime().getMillis()))
                .setParameter((String)"bdt_second", new Timestamp(beginDtSecond.toDateTime().getMillis()))
                .setParameter((String)"edt_second", new Timestamp(endDtSecond.toDateTime().getMillis()))
                .getResultList();

        for (Object[] fa : result) {
            for (int i = 0; i < fa.length; i=i+2) {
                if(getCellData(fa[i])==null)
                    continue;

                if(!getCellData(fa[i]).equals(getCellData(fa[i+1])))
                    return false;
            }
        }
        return true;

    }




    @Override
    public OperationStatus setUrtHeat(UUID uuid,DateTime beginDt, DateTime endDt,DateTime beginSelectDt,DateTime endSelectDt) {

        String queryString = "DECLARE @bdt DATETIME = Cast('"+DateFormat.getDateFormatHrMmSs().format(beginDt.toDate())+"' AS DATETIME)\n" +
                "DECLARE @edt DATETIME = Cast('"+DateFormat.getDateFormatHrMmSs().format(endDt.toDate())+"' AS DATETIME)\n" +
                "\n" +
                "DECLARE @bdt_select DATETIME = Cast('"+DateFormat.getDateFormatHrMmSs().format(beginSelectDt.toDate())+"' AS DATETIME)\n" +
                "DECLARE @edt_select DATETIME = Cast('"+DateFormat.getDateFormatHrMmSs().format(endSelectDt.toDate())+"' AS DATETIME)\n" +
                "\n" +
                "delete from [urt_heat_supply] where dt between @bdt and @edt\n" +
                "insert into [urt_heat_supply](dt,[100 ���]\n" +
                "      ,[21 ���]\n" +
                "      ,[13 ���]\n" +
                "      ,[���])\n" +
                "select CalendarHR.[dt]\n" +
                "      ,[100ata_real] as [100 ���]\n" +
                "      ,[21ata_real] as [21 ���]\n" +
                "      ,[13ata_real] as [13 ���]\n" +
                "      ,[gvs_real] as [���]\n" +
                "from CalendarHR \n" +
                "left join(\n" +
                "          select sum([ata100_supply]) as [100ata_real]\n" +
                "                 ,sum([ata21_supply]) as [21ata_real]\n" +
                "                 ,sum([ata13_supply]) as [13ata_real]\n" +
                "                 ,max([���]) as [gvs_real]\n" +
                "          from(\n" +
                "               select consumer_name\n" +
                "                      ,avg([ata100_supply]) as [ata100_supply]\n" +
                "                      ,avg([ata21_supply]) as [ata21_supply]\n" +
                "                      ,avg([ata13_supply]) as [ata13_supply] \n" +
                "                      ,nullif(avg(THERMOTECHNICS_vol),0)+nullif(avg(PETROCHEMISTRY_vol),0)+nullif(avg(NNK_vol),0)+nullif(avg(department_40th_vol),0) as [���]\n" +
                "               from(\n" +
                "                    select ca.dt AS dt FROM calendarhr ca WHERE ca.dt BETWEEN @bdt_select AND @edt_select) cal                           \n" +
                "               LEFT JOIN (\n" +
                "                          select [heat_energy_fact].dt     \n" +
                "                                 ,[ata100_supply]     \n" +
                "                                 ,[ata13_supply]\n" +
                "                                 ,[ata21_supply]\n" +
                "                                 ,[heat_energy_fact].consumer_name \n" +
                "                                 ,THERMOTECHNICS_vol\n" +
                "                                 ,PETROCHEMISTRY_vol\n" +
                "                                 ,NNK_vol\n" +
                "                                 ,department_40th_vol\n" +
                "                          from [dbo].[heat_energy_fact]\n" +
                "                          left join md_fact_heat on [heat_energy_fact].dt = md_fact_heat.dt\n" +
                "                          )fact ON cal.dt = fact.dt\n" +
                "               group by consumer_name\n" +
                "               )s1\n" +
                "          )s2\n" +
                "on 1=1\n" +
                "where CalendarHR.dt between @bdt and @edt";

        try {
            int code = execute(queryString);
            return recalcURT(beginDt,endDt);
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }


    }

    @Override
    public OperationStatus copyHour(UUID uuid, CommonRestService commonRestService, DateTime dateTime, Integer hour) {

        String declareDates = getDeclareDates(dateTime, hour);
        String deleteFromUrtGeneratorSQL = "delete from [dbo].[urt_generator]\n" +
                "where [dt] between @toBdt and @toEdt and [urt_type] like 'prognosis%'\n";

        String insertIntoUrtGeneratorSQL = getInsertIntoUrtGeneratorSQL("prognosis", dateTime, hour);

        String deleteFromUrtParameters = "delete from [dbo].[urt_parameters]\n" +
                "where [dt] between @toBdt and @toEdt and [urt_type] like 'prognosis%'\n";

        String insertIntoUrtParameters = "";
        for (int hourTo = hour + 1; hourTo < 24; hourTo++) {
            insertIntoUrtParameters += getInsertIntoUrtParameresSQL(getCastDateTimeSql(dateTime, hourTo));
        }

        String deleteSql = declareDates + deleteFromUrtGeneratorSQL;// + "\n" + deleteFromUrtParameters;
        String insertSql = "DECLARE @fromDt DATETIME = " + getCastDateTimeSql(dateTime, hour) + "\n"
                + insertIntoUrtGeneratorSQL;// + "\n" + insertIntoUrtParameters;

        try {
            int code = execute(insertSql, deleteSql);
            List<String> urtTypes = getVariants(dateTime.withHourOfDay(hour));
            OperationStatus result = OperationStatus.ERROR;
            for (String uType : urtTypes) {
                result = recalcUrtVariantDay(dateTime, dateTime, "'" + uType + "'");
            }
            return result;
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }

    }



    private OperationStatus recalcURT(DateTime beginDate, DateTime endDate) {
        try {
            int code = execute("exec [dbo].[urt_calculate]  '" + EPDateFormat.format(beginDate) + " 00:00:00' , '" + EPDateFormat.format(endDate) + " 23:00:00', 'prognosis'");
            return OperationStatus.OK;
        } catch (Exception e) {
            LOG.info(ErrorCode.URT_RECALC_ERROR.getMessage(e.getMessage()));
            return OperationStatus.ERROR;
        }

    }
    private OperationStatus recalcURT(String beginDate, String endDate) {
        try {

            DateTime beginDt = EPDateFormat.dateTimeParse(beginDate);
            DateTime endDt = EPDateFormat.dateTimeParse(endDate);
            return recalcURT(beginDt,endDt);
        } catch (ParseException e) {
            LOG.info(ErrorCode.DT_PARSE_ERROR.getMessage(e.getMessage()));
            return OperationStatus.ERROR;
        }

    }



    private String getBasicQuery(Integer generatorType) {
        String urtType = (generatorType==0) ? "'prognosis'" : (generatorType==1) ? "'min_max'":"";

        return "select datepart(hour,dt) as hour\n" +
                "       ,isnull([100 ���],@null_editor) as [100 ���]\n" +
                "       ,isnull([21 ���],@null_editor) as [21 ���]\n" +
                "       ,isnull([13 ���],@null_editor) as [13 ���]\n" +
                "       ,isnull([���],@null_editor) as [���]\n" +
                "       ,isnull([������ ����������],@null_editor) as [������ ����������]\n" +
                "       ,isnull([b��],@null_editor) as [b��]\n" +
                "       ,isnull([��� �����],@null_editor) as [��� �����]\n" +
                "       ,isnull([���1],@null_editor) as [���1]\n" +
                "       ,isnull([���2],@null_editor) as [���2]\n" +
                "       ,isnull([���� �� ���],@null_editor) as [���� �� ���]\n" +
                "       ,isnull([���� �� ���],@null_editor) as [���� �� ���]\n" +
                "       ,isnull([��� 140/13],@null_editor) as [��� 140/13]\n" +
                "       ,isnull([��� 140/21],@null_editor) as [��� 140/21]\n" +
                "       ,isnull([��� 140/100],@null_editor) as [��� 140/100]\n" +
                "       ,isnull([������ �����],@null_editor) as [������ �����]\n" +
                "       ,isnull([���],@null_editor) as [���]\n" +
                "       ,isnull([������ ����],@null_editor) as [������ ����]\n" +
                "       ,isnull([������� ����������],@null_editor) as [������� ����������]\n" +
                "       ,isnull([��� ��],@null_editor) as [��� ��]\n" +
                "       ,isnull([��],@null_editor) as [��]\n" +
                "       ,isnull([��],@null_editor) as [��]\n" +
                "       ,isnull([delta Q],@null_editor) as [delta Q]\n" +
                "       ,isnull([��������_��1],@null_editor) as [��������_��1]\n" +
                "       ,isnull([���������������� �����_��1],@null_editor) as [���������������� �����_��1]\n" +
                "       ,isnull([���������������� �����_��1],@null_editor) as [���������������� �����_��1]\n" +
                "       ,isnull([D��_��1],@null_editor) as [D��_��1]\n" +
                "       ,isnull([q���_��1],@null_editor) as [q���_��1]\n" +
                "       ,isnull([q��_��1],@null_editor) as [q��_��1]\n" +
                "       ,isnull([��������_��2],@null_editor) as [��������_��2]\n" +
                "       ,isnull([���������������� �����_��2],@null_editor) as [���������������� �����_��2]\n" +
                "       ,isnull([���������������� �����_��2],@null_editor) as [���������������� �����_��2]\n" +
                "       ,isnull([D��_��2],@null_editor) as [D��_��2]\n" +
                "       ,isnull([q���_��2],@null_editor) as [q���_��2]\n" +
                "       ,isnull([q��_��2],@null_editor) as [q��_��2]\n" +
                "       ,isnull([��������_��3],@null_editor) as [��������_��3]\n" +
                "       ,isnull([���������������� �����_��3],@null_editor) as [���������������� �����_��3]\n" +
                "       ,isnull([���������������� �����_��3],@null_editor) as [���������������� �����_��3]\n" +
                "       ,isnull([D��_��3],@null_editor) as [D��_��3]\n" +
                "       ,isnull([q���_��3],@null_editor) as [q���_��3]\n" +
                "       ,isnull([q��_��3],@null_editor) as [q��_��3]\n" +
                "       ,isnull([��������_��5],@null_editor) as [��������_��5]\n" +
                "       ,isnull([���������������� �����_��5],@null_editor) as [���������������� �����_��5]\n" +
                "       ,isnull([���������������� �����_��5],@null_editor) as [���������������� �����_��5]\n" +
                "       ,isnull([D��_��5],@null_editor) as [D��_��5]\n" +
                "       ,isnull([q���_��5],@null_editor) as [q���_��5]\n" +
                "       ,isnull([q��_��5],@null_editor) as [q��_��5]\n" +
                "       ,isnull([��������_��6],@null_editor) as [��������_��6]\n" +
                "       ,isnull([���������������� �����_��6],@null_editor) as [���������������� �����_��6]\n" +
                "       ,isnull([���������������� �����_��6],@null_editor) as [���������������� �����_��6]\n" +
                "       ,isnull([D��_��6],@null_editor) as [D��_��6]\n" +
                "       ,isnull([q���_��6],@null_editor) as [q���_��6]\n" +
                "       ,isnull([q��_��6],@null_editor) as [q��_��6]\n" +
                "       ,isnull([��������_��7],@null_editor) as [��������_��7]\n" +
                "       ,isnull([���������������� �����_��7],@null_editor) as [���������������� �����_��7]\n" +
                "       ,isnull([���������������� �����_��7],@null_editor) as [���������������� �����_��7]\n" +
                "       ,isnull([D��_��7],@null_editor) as [D��_��7]\n" +
                "       ,isnull([q���_��7],@null_editor) as [q���_��7]\n" +
                "       ,isnull([q��_��7],@null_editor) as [q��_��7]\n" +
                "       ,isnull([��������_��8],@null_editor) as [��������_��8]\n" +
                "       ,isnull([���������������� �����_��8],@null_editor) as [���������������� �����_��8]\n" +
                "       ,isnull([���������������� �����_��8],@null_editor) as [���������������� �����_��8]\n" +
                "       ,isnull([D��_��8],@null_editor) as [D��_��8]\n" +
                "       ,isnull([q���_��8],@null_editor) as [q���_��8]\n" +
                "       ,isnull([q��_��8],@null_editor) as [q��_��8]\n" +
                "       ,isnull(cast([���������� ������� ������] as float),@null_editor) as [���������� ������� ������]\n" +
                "       ,isnull([��������],@null_editor) as [��������]\n" +
                "       ,isnull([D��],@null_editor) as [D��]\n" +
                "       ,isnull([q���],@null_editor) as [q���]\n" +
                "       ,isnull([��],@null_editor) as [��]\n" +
                "       ,isnull([D�� �����],@null_editor) as [D�� �����]\n" +
                "       ,isnull([Q��� �����],@null_editor) as [Q��� �����]\n" +
                "       ,isnull([Q��� ���������],@null_editor) as [Q��� ���������]\n" +
                "       ,isnull([Q��],@null_editor) as [Q��]\n" +
                "       ,isnull([q��],@null_editor) as [q��]\n" +
                "       ,isnull([������ � ���],@null_editor) as [������ � ���]\n" +
                "       ,isnull([����],@null_editor) as [����]\n" +
                "       ,isnull([������ ������������ �������],@null_editor) as [������ ������������ �������]\n" +
                "       ,isnull([���],@null_editor) as [���]\n" +
                "       ,isnull([����(�)],@null_editor) as [����(�)]\n" +
                "       ,isnull([����(�)],@null_editor) as [����(�)]\n" +
                "       ,isnull([����-�����],@null_editor) as [����-�����]\n" +
                "       ,isnull([������ � ���_lead],@null_editor) as [������ � ���_lead]\n" +
                "       ,isnull([��� ���],@null_editor) as [��� ���]\n" +
                "       ,isnull([� �� �� ��],@null_editor) as [� �� �� ��]\n" +
                "       ,isnull([��� �� ��],@null_editor) as [��� �� ��]\n" +
                "       ,isnull([����-�����_lead],@null_editor) as [����-�����_lead]\n" +
                "       ,isnull([���],@null_editor) as [���]\n" +
                "       ,isnull([��� ���],@null_editor) as [��� ���]\n" +
                "       ,isnull([� �� �� ��],@null_editor) as [� �� �� ��]\n" +
                "       ,isnull([delta �],@null_editor) as [delta �]\n" +
                "       ,isnull([�],@null_editor) as [�]       \n" +
                "       ,isnull([������������],@null_editor) as [������������]\n" +
                "       ,isnull([���_��1],@null_editor) as [���_��1]\n" +
                "       ,isnull([���_��2],@null_editor) as [���_��2]\n" +
                "       ,isnull([���_��3],@null_editor) as [���_��3]\n" +
                "       ,isnull([���_��5],@null_editor) as [���_��5]\n" +
                "       ,isnull([���_��6],@null_editor) as [���_��6]\n" +
                "       ,isnull([���_��7],@null_editor) as [���_��7]\n" +
                "       ,isnull([���_��8],@null_editor) as [���_��8]\n" +
                "       ,isnull([� �� ��],@null_editor) as [� �� ��]\n" +
                "       ,isnull([��� �],@null_editor) as  [��� �]\n" +
                "       ,isnull([�� �����],@null_editor) as [�� �����]\n" +
                "       ,isnull(cast([������ ��] as float),@null_editor) as [������ ��]\n" +
                "       ,isnull([��� �� ��],@null_editor) as [��� �� ��]\n" +
                "       ,isnull([delta � �],@null_editor) as [delta � �]\n" +
                "       from [urt_calculation] where dt between @bdt and @edt and [urt_type] = " + urtType;
    }

    private String getStationQueryHead() {
        return "--DBCC FREEPROCCACHE WITH NO_INFOMSGS;\n" +
                "--DECLARE @bdt DATETIME = Cast('19.11.2014 00:00:00' AS DATETIME)\n" +
                "--DECLARE @edt DATETIME = Cast('19.11.2014 23:00:00' AS DATETIME)\n" +
                "DECLARE @null_editor integer = 999999999\n" +
                "\n" +
                "DECLARE @bdt DATETIME = :bdt\n" +
                "DECLARE @edt DATETIME = :edt\n" +
                "\n" +
                "\n" +
                "select cast(s25.[column_name] as varchar) as column_name\n" +
                "       ,[0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],[11],[12],[13],[14],[15],[16],[17],[18],[19],[20],[21],[22],[23]        \n" +
                "       ,cast(table_name as varchar) as table_name\n" +
                "       ,orderer\n" +
                "       ,null as tg_id\n" +
                "       ,um\n" +
                "       ,_group\n" +
                "from (\n" +
                "      select column_name\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([0] as integer),[0]),@null_editor) as [0]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([1] as integer),[1]),@null_editor) as [1]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([2] as integer),[2]),@null_editor) as [2]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([3] as integer),[3]),@null_editor) as [3]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([4] as integer),[4]),@null_editor) as [4]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([5] as integer),[5]),@null_editor) as [5]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([6] as integer),[6]),@null_editor) as [6]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([7] as integer),[7]),@null_editor) as [7]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([8] as integer),[8]),@null_editor) as [8]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([9] as integer),[9]),@null_editor) as [9]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([10] as integer),[10]),@null_editor) as [10]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([11] as integer),[11]),@null_editor) as [11]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([12] as integer),[12]),@null_editor) as [12]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([13] as integer),[13]),@null_editor) as [13]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([14] as integer),[14]),@null_editor) as [14]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([15] as integer),[15]),@null_editor) as [15]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([16] as integer),[16]),@null_editor) as [16]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([17] as integer),[17]),@null_editor) as [17]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([18] as integer),[18]),@null_editor) as [18]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([19] as integer),[19]),@null_editor) as [19]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([20] as integer),[20]),@null_editor) as [20]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([21] as integer),[21]),@null_editor) as [21]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([22] as integer),[22]),@null_editor) as [22]\n" +
                "             ,nullif(iif([column_name]= '���������� ������� ������' or [column_name] = '������ ��',cast([23] as integer),[23]),@null_editor) as [23]\n" +
                "      from(\n" +
                "           select column_name,value,hour from(\n";
    }

    private String getStationQueryFoot() {
        return "     )as tmp\n" +
                "UNPIVOT (value FOR column_name in ([������ �����],[100 ���],[21 ���],[13 ���],[���],[������ ����],[������� ����������],[������ ����������],[���],[��� 140/13],[��� 140/21],[��� 140/100],[��],[��],[Q��],[delta Q],[���],[����(�)],[����(�)],[��������],[��],[������ ��],[������ � ���],[D��],[q���],[q��],[����],[������ ������������ �������],[��� �� ��],[��� ��],[��� ���],[��� �� ��],[����-�����],[�],[���],[��� ���],[� �� �� ��],[delta �],[b��],[� �� �� ��],[���������� ������� ������],[Q��� ���������],[Q��� �����],[D�� �����],[��������_��1],[��������_��2],[��������_��3],[��������_��5],[��������_��6],[��������_��7],[��������_��8],[�� �����],[������������],[��� �],[� �� ��],[delta � �])) AS unpvt\n" +
                "    )as tmp\n" +
                "pivot (max(value) for hour in ([0],[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],[11],[12],[13],[14],[15],[16],[17],[18],[19],[20],[21],[22],[23])) p \n" +
                ")s24\n" +
                "left join(\n" +
                "          select 'base' as _group, '������ �����' as column_name, 1 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '������ �����' as _group, '100 ���' as column_name, 2 as orderer,'heat_supply' as table_name, '(����/�)' as um union all\n" +
                "          select '������ �����' as _group, '21 ���' as column_name, 3 as orderer,'heat_supply' as table_name, '(����/�)' as um union all\n" +
                "          select '������ �����' as _group, '13 ���' as column_name, 4 as orderer,'heat_supply' as table_name, '(����/�)' as um union all\n" +
                "          select '������ �����' as _group, '���' as column_name, 5 as orderer,'heat_supply' as table_name, '(����/�)' as um union all\n" +
                "          select '������ �����' as _group, '������ ����' as column_name, 6 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '������ �����' as _group, '������� ����������' as column_name, 7 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '������ �����' as _group, '������ ����������' as column_name, 8 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select 'base' as _group, '���' as column_name, 9 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '���' as _group, '��� 140/13' as column_name, 10 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '���' as _group, '��� 140/21' as column_name, 11 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '���' as _group, '��� 140/100' as column_name, 12 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select 'base' as _group, '��' as column_name, 13 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '��' as _group, '�� �����' as column_name, 14 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select 'base' as _group, '��' as column_name, 15 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '��' as _group, 'Q��' as column_name, 16 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '��' as _group, 'delta Q' as column_name, 17 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '��' as _group, '���' as column_name, 18 as orderer,null as table_name, null as um union all\n" +
                "          select '��' as _group, '����(�)' as column_name, 19 as orderer,null as table_name, null as um union all\n" +
                "          select '��' as _group, '����(�)' as column_name, 20 as orderer,null as table_name, null as um union all\n" +
                "          select 'base' as _group, '��������' as column_name, 21 as orderer,null as table_name, '(����)' as um union all\n" +
                "          select '��������' as _group, '��' as column_name, 22 as orderer,null as table_name, '(����)' as um union all\n" +
                "          select '��������' as _group, '������ ��' as column_name, 23 as orderer,null as table_name, '(��)' as um union all\n" +
                "          select '��������' as _group, '������ � ���' as column_name, 24 as orderer,null as table_name, '(����)' as um union all\n" +
                "          select '��������' as _group, 'D��' as column_name, 25 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '��������' as _group, 'q���' as column_name, 26 as orderer,null as table_name, '(����/����)' as um union all\n" +
                "          select '��������' as _group, 'q��' as column_name, 27 as orderer,null as table_name, '(����/����)' as um union all\n" +
                "          select 'base' as _group, '����' as column_name, 28 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '������ ������������ �������' as column_name, 29 as orderer,null as table_name, '(��3)' as um union all\n" +
                "          select '����' as _group, '��� �� ��' as column_name,30 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '��� �� ��' as column_name, 31 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '��� ���' as column_name, 32 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '��� ��' as column_name, 33 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '����-�����' as column_name, 34 as orderer,null as table_name, '(���/�)' as um union all\n" +
                "          select '����' as _group, '� �� ��' as column_name, 35 as orderer,null as table_name, '(��3)' as um union all\n" +
                "          select '����' as _group, '�' as column_name, 36 as orderer,null as table_name, null as um union all\n" +
                "          select 'base' as _group, '���' as column_name, 37 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select '���' as _group, '��� �' as column_name, 38 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select '���' as _group, '��� ���' as column_name, 39 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select '���' as _group, '� �� �� ��' as column_name, 40 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select '���' as _group, 'delta �' as column_name, 41 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select '���' as _group, 'delta � �' as column_name, 42 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select 'base' as _group, 'b��' as column_name, 43 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select 'b��' as _group, '� �� �� ��' as column_name, 44 as orderer,null as table_name, '(��/����)' as um union all\n" +
                "          select 'base' as _group, '���������� ������� ������' as column_name, 45 as orderer,'boilers' as table_name, '(��)' as um union all\n" +
                "          select '���������� ������� ������' as _group, 'Q��� ���������' as column_name, 46 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '���������� ������� ������' as _group, 'Q��� �����' as column_name, 47 as orderer,null as table_name, '(����/�)' as um union all\n" +
                "          select '���������� ������� ������' as _group, 'D�� �����' as column_name, 48 as orderer,null as table_name, '(�/�)' as um union all\n" +
                "          select '���������� ������� ������' as _group, '������������' as column_name, 49 as orderer,null as table_name, '(�����/��)' as um union all\n" +
                "          select null as _group, '��������_��1' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��2' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��3' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��5' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��6' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��7' as column_name, null as orderer, null as table_name, null as um union all " +
                "          select null as _group, '��������_��8' as column_name, null as orderer, null as table_name, null as um " +
                "          )s25\n" +
                "on s24.column_name = s25.column_name\n" +
                "order by orderer";
    }

    private List<PrognosisUrtRecord> getPrognosisUrtRecordList(String queryString, DateTime dt) {
        List<PrognosisUrtRecord> responseRecords = new ArrayList<PrognosisUrtRecord>(0);


        LocalDateTime bdt = dt.withTime(0, 0, 0, 0).toLocalDateTime();
        LocalDateTime edt = dt.withTime(23, 0, 0, 0).toLocalDateTime();


        Query q = getEntityManager().createNativeQuery(queryString);
        List<Object[]> result = q.setParameter("bdt", new Timestamp(bdt.toDateTime().getMillis()))
                .setParameter("edt", new Timestamp(edt.toDateTime().getMillis())).getResultList();

        for (Object[] fa : result) {
            PrognosisUrtRecord record = new PrognosisUrtRecord();
            record.columnName = getCellData(fa[0]);
            record.value0 = getCellData(fa[1]);
            record.value1 = getCellData(fa[2]);
            record.value2 = getCellData(fa[3]);
            record.value3 = getCellData(fa[4]);
            record.value4 = getCellData(fa[5]);
            record.value5 = getCellData(fa[6]);
            record.value6 = getCellData(fa[7]);
            record.value7 = getCellData(fa[8]);
            record.value8 = getCellData(fa[9]);
            record.value9 = getCellData(fa[10]);
            record.value10 = getCellData(fa[11]);
            record.value11 = getCellData(fa[12]);
            record.value12 = getCellData(fa[13]);
            record.value13 = getCellData(fa[14]);
            record.value14 = getCellData(fa[15]);
            record.value15 = getCellData(fa[16]);
            record.value16 = getCellData(fa[17]);
            record.value17 = getCellData(fa[18]);
            record.value18 = getCellData(fa[19]);
            record.value19 = getCellData(fa[20]);
            record.value20 = getCellData(fa[21]);
            record.value21 = getCellData(fa[22]);
            record.value22 = getCellData(fa[23]);
            record.value23 = getCellData(fa[24]);
            record.tableName = getCellData(fa[25]);
            record.orderer = getCellData(fa[26]);
            record.tgId = getCellData(fa[27]);
            record.um = getCellData(fa[28]);
            record._group = getCellData(fa[29]);
            responseRecords.add(record);
        }

        return responseRecords;
    }

    private String getCurrentParameterUpdateString(PrognosisUrtParametersRecord record){
        String urtType = (record.calculateType==0) ? "'prognosis'" : (record.calculateType==1) ? "'min_max'":"";

        String hr = String.valueOf(record.hour - 1);
        String dt = " Cast('" + record.dt + " " + hr + ":00:00' AS DATETIME)";

        String condensateLosses = (record.condensateLosses != null) ? getSqlFloatData(record.condensateLosses) : "NULL";
        String coeffBte = (record.coeffBte != null) ? getSqlFloatData(record.coeffBte) : "NULL";
        String boilerEfficiency = (record.boilerEfficiency != null) ? getSqlFloatData(record.boilerEfficiency) : "NULL";
        String kTv1 = (record.kTv1 != null) ? getSqlFloatData(record.kTv1) : "NULL";
        String kTv2 = (record.kTv2 != null) ? getSqlFloatData(record.kTv2) : "NULL";
        String coeffSnEtl = (record.coeffSnEtl != null) ? getSqlFloatData(record.coeffSnEtl) : "NULL";
        String coeffSnEel = (record.coeffSnEel != null) ? getSqlFloatData(record.coeffSnEel) : "NULL";
        String rou100 = (record.rou100 != null) ? record.rou100.toString() : "NULL";
        String rou21 = (record.rou21 != null) ? record.rou21.toString() : "NULL";
        String rou13 = (record.rou13 != null) ? record.rou13.toString() : "NULL";
        String caloricContent = (record.caloricContent != null) ? record.caloricContent.toString() : "NULL";
        String bUslKindling = (record.bUslKindling != null) ? record.bUslKindling.toString() : "NULL";
        String bTeNrPr = (record.bTeNrPr != null) ? record.bTeNrPr.toString() : "NULL";

        String sql = "DECLARE @dt DATETIME = " + dt + "\n" +
                "DECLARE @urt_type varchar(max) = " + urtType + "\n"+
                "begin tran\n" +
                "   UPDATE [dbo].[urt_parameters]\n" +
                "   SET \n";

        switch (record.column){
            case "condensateLosses":
                sql += "       [������ ����������] = " + condensateLosses + "\n";
                break;
            case "coeffBte":
                sql += "       [���� b��] = " + coeffBte + "\n";
                break;
            case "boilerEfficiency":
                sql += "       [��� �����] = " + boilerEfficiency + "\n";
                break;
            case "kTv1":
                sql += "       [���1] = " + kTv1 + "\n";
                break;
            case "kTv2":
                sql += "       [���2] = " + kTv2 + "\n";
                break;
            case "coeffSnEtl":
                sql += "       [���� �� ���] = " + coeffSnEtl + "\n";
                break;
            case "coeffSnEel":
                sql += "       [���� �� ���] = " + coeffSnEel + "\n";
                break;
            case "rou100":
                sql += "       [��� 100] = " + rou100 + "\n";
                break;
            case "rou21":
                sql += "       [��� 21] = " + rou21 + "\n";
                break;
            case "rou13":
                sql += "       [��� 13] = " + rou13 + "\n";
                break;
            case "caloricContent":
                sql += "       [������������] = " + caloricContent + "\n";
                break;
            case "bUslKindling":
                sql += "       [���� ��������] = " + bUslKindling + "\n";
                break;
            case "bTeNrPr":
                sql += "       [� �� �� ��] = " + bTeNrPr + "\n";
                break;
        }

        sql +=  "   where dt = @dt and [urt_type] = @urt_type\n" +
                "\n" +
                "   if @@rowcount = 0\n" +
                "   begin\n" +
                "      INSERT INTO [dbo].[urt_parameters]([dt],[urt_type],";

        switch (record.column){
            case "condensateLosses":
                sql += "[������ ����������])\n select @dt,@urt_type, "+condensateLosses +"\n   end\n commit tran";
                break;
            case "coeffBte":
                sql += "[���� b��])\n select @dt,@urt_type, "+coeffBte +"\n   end\n commit tran";
                break;
            case "boilerEfficiency":
                sql += "[��� �����])\n select @dt,@urt_type, "+boilerEfficiency +"\n   end\n commit tran";
                break;
            case "kTv1":
                sql += "[���1])\n select @dt,@urt_type, "+kTv1 +"\n   end\n commit tran";
                break;
            case "kTv2":
                sql += "[���2])\n select @dt,@urt_type, "+kTv2 +"\n   end\n commit tran";
                break;
            case "coeffSnEtl":
                sql += "[���� �� ���])\n select @dt,@urt_type, "+coeffSnEtl +"\n   end\n commit tran";
                break;
            case "coeffSnEel":
                sql += "[���� �� ���])\n select @dt,@urt_type, "+coeffSnEel +"\n   end\n commit tran";
                break;
            case "rou100":
                sql += "[��� 100])\n select @dt,@urt_type, "+rou100 +"\n   end\n commit tran";
                break;
            case "rou21":
                sql += "[��� 21])\n select @dt,@urt_type, "+rou21 +"\n   end\n commit tran";
                break;
            case "rou13":
                sql += "[��� 13])\n select @dt,@urt_type, "+rou13 +"\n   end\n commit tran";
                break;
            case "caloricContent":
                sql += "[������������] )\n select @dt,@urt_type, "+caloricContent +"\n   end\n commit tran";
                break;
            case "bUslKindling":
                sql += "[���� ��������] )\n select @dt,@urt_type, "+bUslKindling +"\n   end\n commit tran";
                break;
            case "bTeNrPr":
                sql += "[� �� �� ��] )\n select @dt,@urt_type, "+bTeNrPr +"\n   end\n commit tran";
                break;
        }

        return sql;
    }

    private OperationStatus execStoredProcedureString(PrognosisUrtSetRecord prognosisUrtDataRecord, PrognosisUrtParametersRecord parametersRecord) throws Exception {
        if((prognosisUrtDataRecord==null && parametersRecord==null) || (prognosisUrtDataRecord!=null && parametersRecord!=null)){
            throw new Exception("���������� ��������� ����������: ������������ ���������");
        }
        DateTime prognosisEndDate = new DateTime().plusDays(3);

        String bdt = "";
        String edt = "";
        String urtType = "";

        if(prognosisUrtDataRecord!=null){
            bdt = prognosisUrtDataRecord.dt;
            edt = prognosisUrtDataRecord.dt;
        }else {
            bdt = parametersRecord.dt;
            edt = getEdtOnParametersChange(parametersRecord);
            edt = (!"".equals(edt))? edt : prognosisEndDate.getDayOfMonth()+"."+prognosisEndDate.getMonthOfYear()+"."+prognosisEndDate.getYear();
        }

        return recalcURT(bdt,edt);

    }

    private String getEdtOnParametersChange(PrognosisUrtParametersRecord record){
        String columnName = "";
        String urtType = (record.calculateType==0)? "'prognosis'" : (record.calculateType==1) ? "'min_max'":"";
        switch (record.column){
            case "condensateLosses":
                columnName = "[������ ����������]";
                break;
            case "coeffBte":
                columnName = "[���� b��]";
                break;
            case "boilerEfficiency":
                columnName = "[��� �����]";
                break;
            case "kTv1":
                columnName = "[���1]";
                break;
            case "kTv2":
                columnName = "[���2]";
                break;
            case "coeffSnEtl":
                columnName = "[���� �� ���]";
                break;
            case "coeffSnEel":
                columnName = "[���� �� ���]";
                break;
            case "rou100":
                columnName = "[��� 100]";
                break;
            case "rou21":
                columnName = "[��� 21]";
                break;
            case "rou13":
                columnName = "[��� 13]";
                break;
            case "caloricContent":
                columnName = "[������������] ";
                break;
            case "bUslKindling":
                columnName = "[���� ��������] ";
                break;
            case "bTeNrPr":
                columnName = "[� �� �� ��] ";
                break;
        }
        String sql ="select top(1) dt,"+columnName+" from [urt_parameters] where [urt_type]="+urtType+" and "+columnName + " is not null and dt > cast('"+record.dt +" "+ (record.hour-1)+":00:00' as datetime)";

        String edt = "";
        try{
            Query q = getEntityManager().createNativeQuery(sql);
            List<Object[]> result = q.getResultList();
            if(result.size()>0){
                edt = getCellData(result.get(0)[0],DateFormat.getDateFormat());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return edt;
    }













    /**
     * ���������� ������ ��������� ��������� �� ��� ����
     * ���� {'prognosis', 'prognosis2'}
     * @param fromDt
     * @return
     */
    protected List<String> getVariants(DateTime fromDt) {

        String castDt = getCastDateTimeSql(fromDt, 0);

        String queryString =
                "DECLARE @bdt DATETIME = " + castDt + "\n" +
                "\n" +
                "select DISTINCT [urt_type] from [urt_calculation]\n" +
                "where dt = @bdt and [urt_type] like ('prognosis%') --and ([��������_��1] is not null or [��������_��2] is not null or [��������_��3] is not null or [��������_��5] is not null or [��������_��6] is not null or [��������_��7] is not null or [��������_��8] is not null)";
        Query q = getEntityManager().createNativeQuery(queryString);

        List<String> resultList = q.getResultList();
        Collections.sort(resultList);
        return resultList;
    }

    /**
     * ���������� ������ � sql-��������, ��� ��������� ������ �� [urt_calculation]
     * @param urtTypes
     * @return
     */
    private String getQueryString(List<String> urtTypes) {
        int order = 0;
        String queryString =
                "--DECLARE @bdt DATETIME = Cast('12.08.2015 00:00:00' AS DATETIME)\n" +
                        "DECLARE @bdt DATETIME = :bdt\n" +
                        "\n" +
                        "select orderr, m1, mu, tgGroup \n";
        for (String type : urtTypes) {
            queryString += ", " + type;
        }
        queryString += " from (\n";

        int prognosisNumb = 0;
        for (String type : urtTypes) {
            prognosisNumb++;

            queryString += "\n" +
                    "select cast('��������� ��������' as varchar) as p" + prognosisNumb + ", (isnull([��������_��1],0) + isnull([��������_��2],0) + isnull([��������_��3],0) + isnull([��������_��5],0) + isnull([��������_��6],0) + isnull([��������_��7],0) + isnull([��������_��8],0)) as " + type + "\n" +
                    "from [urt_calculation] where dt = @bdt and [urt_type] = '" + type + "'\n";
            for (int tg_num = 1; tg_num <= 8; tg_num++) {
                if (tg_num == 4) continue;
                queryString += "union all\n" +
                        "select cast(p" + prognosisNumb + " as varchar)," + type + " from (\n" +
                        "    select isnull([��������_��" + tg_num + "],0) as [��������_��" + tg_num + "], isnull([���������������� �����_��"+tg_num+"],0) as ["+(tg_num==3||tg_num==6?"���������������_��":"���������������� �����_��") + tg_num + "], isnull([���������������� �����_��" + tg_num + "],0) as [���������������� �����_��" + tg_num + "], isnull([D��_��" + tg_num + "],0) as [D��_��" + tg_num + "], isnull([q���_��" + tg_num + "],0) as [q���_��" + tg_num + "], isnull([q��_��" + tg_num + "],0) as [q��_��" + tg_num + "], isnull([���_��" + tg_num + "],0) as [���_��" + tg_num + "]\n" +
                        "    from [urt_calculation] where dt = @bdt and [urt_type] = '" + type + "' ) as p" + prognosisNumb + "\n" +
                        "UNPIVOT (" + type + " FOR p" + prognosisNumb + " in ( [��������_��" + tg_num + "],["+(tg_num==3||tg_num==6?"���������������_��":"���������������� �����_��") + tg_num + "],[���������������� �����_��" + tg_num + "],[D��_��" + tg_num + "],[q���_��" + tg_num + "],[q��_��" + tg_num + "],[���_��" + tg_num + "])) AS unpvt\n";
            }

            queryString += ") s" + prognosisNumb + (prognosisNumb == 1 ? "" : " on p" + prognosisNumb + " = p" + (prognosisNumb - 1)) + "\n" +
                    "\n" +
                    "left join\n" +
                    "(";
        }

        queryString += getMeasureQuery(order) + "\n) ms2 " + ((urtTypes.size() > 0) ? "on p1=m1\n" : "\n") +
                "order by orderr\n";
        return queryString;
    }

    /**
     * ������ ���������� [urt_calculation] ��� �������� ���, ���� � ���� ���
     * @param date
     * @param hr (�� 0 �� 23)
     * @return
     */
    private OperationStatus recalcUrtVariantHour(String date, String hr, String variant) {
        try {
            DateTime beginDt = EPDateFormat.dateTimeParse(date);
            return recalcUrtVariantHour(beginDt, hr, variant);
        } catch (ParseException e) {
            LOG.error(ErrorCode.DT_PARSE_ERROR.getMessage(e.getMessage()));
            return OperationStatus.ERROR;
        }
    }

    /**
     * ������ ���������� [urt_calculation] ��� �������� ���, ���� � ���� ���
     * @param date
     * @param hr (�� 0 �� 23)
     * @return
     */
    private OperationStatus recalcUrtVariantHour(DateTime date, String hr, String variant) {
        try {
            String sql = "exec [dbo].[urt_calculate]  '" + EPDateFormat.format(date) + " " + hr + ":00:00' , '" + EPDateFormat.format(date) + " "
                            + hr + ":00:00', '"+variant +"'";
            int code = execute(sql);
            return OperationStatus.OK;
        } catch (Exception e) {
            LOG.error(ErrorCode.URT_RECALC_ERROR.getMessage(e.getMessage()));
            return OperationStatus.ERROR;
        }
    }

    /**
     * ���������� DateTime �� "dd.MM.yyyy"-������ � ����
     * @param dt
     * @param hour (�� 0 �� 23)
     * @return
     */
    protected DateTime getDateTime(String dt, Integer hour) {
        if (hour < 0 || hour > 23 || !dt.matches("(0?[1-9]|[12][0-9]|3[01]).(0?[1-9]|1[012]).((19|20)\\d\\d)") ){
            throw new IllegalArgumentException("getDateTime(): dt = " + dt + " hour = " + hour);
        };
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        return formatter.parseDateTime(dt + " " + hour + ":00:00");
    }


    private String getCurrentVariantUrtType(Integer variantNumber) {
        if (variantNumber == 1)
            return "prognosis";
        else
            return "prognosis" + variantNumber;

    }

    private String getNextVariantUrtType(Integer variantNumber) {
        return "prognosis" + (variantNumber + 1);
    }

    private String getPreviousVariantUrtType(Integer variantNumber) {
        if (variantNumber == 1)
            throw new IllegalArgumentException("� ������� �������� �� ����� ���� �����������.");
        if (variantNumber == 2)
            return "prognosis";
        else //if (variantNumber > 2)
            return "prognosis" + (variantNumber - 1);
    }

    private OperationStatus recalcUrtVariantDay(DateTime beginDate, DateTime endDate, String urtType) {
        try {
            int code = execute("exec [dbo].[urt_calculate]  '" + EPDateFormat.format(beginDate) + " 00:00:00' , '" + EPDateFormat.format(endDate) + " 23:00:00', " + urtType);
            return OperationStatus.OK;
        } catch (Exception e) {
            LOG.error(ErrorCode.URT_RECALC_ERROR.getMessage(e.getMessage()));
            return OperationStatus.ERROR;
        }

    }


    private OperationStatus recalcUrtVariants(DateTime beginDate, DateTime endDate, List<String> variants) {
        OperationStatus operationStatus = OperationStatus.ERROR;
        for (String variant : variants) {
            operationStatus = recalcUrtVariantDay(beginDate, endDate, variant);
        }
        return operationStatus;
    }

    private String getMeasureQuery(int order) {
        String measure = "select cast('��������� ��������' as varchar) as m1, cast('(����)' as varchar) as mu, " + order++ + " as orderr, '��������' as tgGroup ";
        for (int tg_num = 1; tg_num <= 8; tg_num++) {
            if (tg_num == 4) continue;
            measure += "union all\n" +
                    "select cast('��������_��" + tg_num + "' as varchar) as m1, cast('(����)' as varchar) as mu, " + order++ + " as orderr, '��������' as tgGroup union all\n" +
                    "    select cast('"+(tg_num==3||tg_num==6?"���������������_��":"���������������� �����_��") + tg_num + "' as varchar) as m1, cast('(����/�)' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup union all\n" +
                    "    select cast('���������������� �����_��" + tg_num + "' as varchar) as m1, cast('(����/�)' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup union all\n" +
                    "    select cast('D��_��" + tg_num + "' as varchar) as m1, cast('(�/�)' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup union all\n" +
                    "    select cast('q���_��" + tg_num + "' as varchar) as m1, cast('(����/����)' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup union all\n" +
                    "    select cast('q��_��" + tg_num + "' as varchar) as m1, cast(' ' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup union all\n" +
                    "    select cast('���_��" + tg_num + "' as varchar) as m1, cast('(��/����)' as varchar) as mu, " + order++ + " as orderr, '��������_��"+tg_num+"' as tgGroup ";
        }
        return measure;
    }



    private String copyVariantIntoUrtGenerator(String urtTypeTo, String urtTypeFrom) {

        String sql =
                "insert into [dbo].[urt_generator] ([dt],[��������],[���������������� �����],[���������������� �����],[id ��],[urt_type])\n" +
                        "select  [dt]\n" +
                        "      ,[��������]  \n" +
                        "      ,[���������������� �����]\n" +
                        "      ,[���������������� �����]\n" +
                        "      ,[id ��]\n" +
                        "      ,[urt_type]\n" +
                        "from (\n";

        for (int tgNumb = 1; tgNumb <= 8; tgNumb++) {
            if (tgNumb == 4) continue;

            int tgId = getTgId(tgNumb);
            sql +=  "SELECT\n" +
                    "       [dt]\n" +
                    "      ,[��������_��" + tgNumb + "] as [��������]\n" +
                    "      ,[���������������� �����_��" + tgNumb + "] as [���������������� �����]\n" +
                    "      ,[���������������� �����_��" + tgNumb + "] as [���������������� �����]\n" +
                    "      , " + tgId + " as [id ��]\n" +
                    "      ,'" + urtTypeTo + "' as [urt_type]\n" +
                    "FROM [dbo].[urt_calculation]\n" +
                    "where dt = @dt  and [urt_type] = '" + urtTypeFrom + "'\n" +
                    "union all\n";
        }

        sql = sql.substring(0, sql.length() - 11);
        sql += "\n) s1 where [��������] is not null or [���������������� �����] is not null or [���������������� �����] is not null\n\n\n";

        return sql;

    }

    protected String copyVariantIntoUrtParameters(String urtTypeTo, String urtTypeFrom) {

        return
                "insert into [dbo].[urt_parameters] ([��� �����],[���1],[���2],[���� b��],[���� �� ���],[���� �� ���],[������ ����������],[dt],[��� 100],[��� 13],[��� 21],[������������],[���� ��������],[� �� �� ��],[urt_type])\n" +
                        "select " + formatParameter("��� �����") +
                        "       ,"+ formatParameter("���1") +
                        "       ,"+ formatParameter("���2") +
                        "       ,"+ formatParameter("���� b��") +
                        "       ,"+ formatParameter("���� �� ���") +
                        "       ,"+ formatParameter("���� �� ���") +
                        "       ,"+ formatParameter("������ ����������") +
                        "       ,@dt_param as [dt]\n" +
                        "       ,"+ formatParameter("��� 100") +
                        "       ,"+ formatParameter("��� 13") +
                        "       ,"+ formatParameter("��� 21") +
                        "       ,"+ formatParameter("������������") +
                        "       ,"+ formatParameter("���� ��������") +
                        "       ,"+ formatParameter("� �� �� ��") +
                        "       ,'"+urtTypeTo+"' as [urt_type] \n" +
                        "from (\n" +
                        "  SELECT [��� �����],[���1],[���2],[���� b��],[���� �� ���],[���� �� ���],[������ ����������],[dt],[��� 100],[��� 13],[��� 21],[������������],[���� ��������],[� �� �� ��],[urt_type]\n" +
                        "  FROM [dbo].[urt_parameters]\n" +
                        "  where dt = @dt_param and [urt_type] = '"+urtTypeFrom+"' ) s\n" +
                        joinParameter("��� �����",urtTypeFrom,1) + "\n" +
                        joinParameter("���1",urtTypeFrom,2) + "\n" + //todo top 10
                        joinParameter("���2",urtTypeFrom,3) + "\n" +
                        joinParameter("���� b��",urtTypeFrom,4) + "\n" +
                        joinParameter("���� �� ���",urtTypeFrom,5) + "\n" +
                        joinParameter("���� �� ���",urtTypeFrom,6) + "\n" +
                        joinParameter("������ ����������",urtTypeFrom,7) + "\n" +
                        joinParameter("��� 100",urtTypeFrom,8) + "\n" +
                        joinParameter("��� 13",urtTypeFrom,9) + "\n" +
                        joinParameter("��� 21",urtTypeFrom,10) + "\n" +
                        joinParameter("������������",urtTypeFrom,11) + "\n" +
                        joinParameter("���� ��������",urtTypeFrom,12) + "\n" +
                        joinParameter("� �� �� ��",urtTypeFrom,13) + "\n" +
                        "";
    }

    private String formatParameter(String parameter) {
        return "coalesce(["+parameter+"],["+parameter+" last]) as ["+parameter+"]\n";
    }

    private String joinParameter(String parameter, String urtTypeFrom, int index) {
        return  "  full join (\n" +
                "      select top 1 ["+parameter+"] as ["+parameter+" last] from [urt_parameters]\n" +
                "      where ["+parameter+"] is not null and [urt_parameters].urt_type = '"+urtTypeFrom+"'\n" +
                "      order by dt desc) s"+index+" on 1=1\n";
    }

    private String getInsertIntoUrtGeneratorSQL(String urtType, DateTime dt, Integer hour) {
        String insert = "insert into [dbo].[urt_generator] ([dt],[��������],[���������������� �����],[���������������� �����],[id ��],[urt_type])\n";
        String sql = "";

        for (int toHour = hour+1; toHour < 24; toHour++) {
            sql += insert + getSelectToHour(urtType, dt, toHour);

            sql = sql.substring(0, sql.length() - 11);
            sql += "\n) s1 where [��������] is not null or [���������������� �����] is not null or [���������������� �����] is not null\n\n";
        }

        return sql;
    }

    private String getSelectToHour(String urtType, DateTime dt, int toHour) {
        String sql = "" +
                "select  [dt]\n" +
                "      ,[��������]  \n" +
                "      ,[���������������� �����]\n" +
                "      ,[���������������� �����]\n" +
                "      ,[id ��]\n" +
                "      ,[urt_type]\n" +
                "from (\n";

        for (int tgNumb = 1; tgNumb <= 8; tgNumb++) {
            if (tgNumb == 4) continue;

            int tgId = getTgId(tgNumb);

            sql +=  "SELECT\n" +
                    "       " + getCastDateTimeSql(dt, toHour) + " as [dt]\n" +
                    "      ,[��������_��" + tgNumb + "] as [��������]\n" +
                    "      ,[���������������� �����_��" + tgNumb + "] as [���������������� �����]\n" +
                    "      ,[���������������� �����_��" + tgNumb + "] as [���������������� �����]\n" +
                    "      , " + tgId + " as [id ��]\n" +
                    "      ,[urt_type] as [urt_type]\n" +
                    "FROM [dbo].[urt_calculation]\n" +
                    "where dt = @fromDt  and [urt_type] like '"+urtType+"%'\n" +
                    "union all\n";
        }
        return sql;
    }

    private int getTgId(int tgNumb) {
        if (tgNumb < 1 || tgNumb > 8 || tgNumb == 4)
            throw new IllegalArgumentException("tgNumb ������ ���� �� 1 �� 3 ��� �� 5 �� 8 ������������");
        if (tgNumb < 4)
            return tgNumb + 1;
        else if (tgNumb == 5)
            return 5;
        else if (tgNumb == 6)
            return 176;
        else //if (tgNumb >= 7)
            return tgNumb - 1;
    }

    private String getDeclareDates(DateTime dt, Integer hr) {
        return "DECLARE @fromDt DATETIME = " + getCastDateTimeSql(dt, hr) + "\n" +
                "DECLARE @toBdt DATETIME = " + getCastDateTimeSql(dt, hr+1) + "\n" +
                "DECLARE @toEdt DATETIME = " + getCastDateTimeSql(dt, 23) + "\n";
    }

    private String getCastDateTimeSql(DateTime dateTime, Integer hour) {
        String dtStr = DateFormat.getDateFormat().format(dateTime.toDate());
        return "Cast('" + dtStr + " " + hour + ":00:00' AS DATETIME)";
    }


    private String getInsertIntoUrtParameresSQL(String dateTimeTo) {
        String sql = "" +
                "insert into [dbo].[urt_parameters] ([��� �����],[���1],[���2],[���� b��],[���� �� ���],[���� �� ���],[������ ����������],[dt],[��� 100],[��� 13],[��� 21],[������������],[���� ��������],[� �� �� ��],[urt_type])\n" +
                "select [��� �����],[���1],[���2],[���� b��],[���� �� ���],[���� �� ���],[������ ����������]," + dateTimeTo + "as[dt],[��� 100],[��� 13],[��� 21],[������������],[���� ��������],[� �� �� ��],[urt_type]\n" +
                "from [dbo].[urt_parameters]\n" +
                "where dt = @fromDt\n";
        return sql;
    }

    private void setData(PrognosisUrtSetRecord record) {

        String tgUpdate = record.tgId == null ? "" : "and [id ��] = "+record.tgId;
        String tgInsert = record.tgId == null ? "" : ",[id ��]";
        String tgValue = record.tgId == null ? "" : ","+record.tgId;
        String isNotNull = " =  @urt_type";


        String hr = String.valueOf(record.hour);
        String dt = " Cast('" + record.dt + " " + hr + ":00:00' AS DATETIME)";

        String tableName = "urt_generaor";
        String value = (record.value != null) ? getSqlFloatData(record.value) : "NULL";
        String valueName = record.valueName;
        if (valueName.startsWith("���������������"))
            valueName = valueName.replace("���������������","���������������� �����");

        String sql = "DECLARE @dt DATETIME = " + dt + "\n" +
                "DECLARE @urt_type varchar(max) = 'prognosis'\n" +
                "begin tran\n" +
                "   UPDATE [dbo]." + tableName + "\n" +
                "   SET \n" +
                "["+valueName + "]=" + value +
                "   where dt = @dt " + tgUpdate + " and [urt_type] "+isNotNull+"\n" +
                "\n" +
                "   if @@rowcount = 0\n" +
                "   begin\n" +
                "      INSERT INTO [dbo]." + tableName + "([dt],[urt_type],[" + valueName + "]"+tgInsert+")\n" +
                "      select @dt,@urt_type," + value + tgValue + "\n" +
                "   end\n" +
                "commit tran";
        try {
            int code = execute(sql);
        } catch (Exception e) {
            LOG.info(ErrorCode.EDIT_DATA.getMessage(e.getMessage()));
            throw new SecurityException(ErrorCode.EDIT_DATA.getMessage());
        }
    }

}
