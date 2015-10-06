package ru.sanors.energy_portal.sc.web_portal.prognosis_urt;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: zybinra
 * Date: 25.11.14
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public class PrognosisUrtRecord implements Serializable{
    public String columnName;

    public Double value0;
    public Double value1;
    public Double value2;
    public Double value3;
    public Double value4;
    public Double value5;
    public Double value6;
    public Double value7;
    public Double value8;
    public Double value9;
    public Double value10;
    public Double value11;
    public Double value12;
    public Double value13;
    public Double value14;
    public Double value15;
    public Double value16;
    public Double value17;
    public Double value18;
    public Double value19;
    public Double value20;
    public Double value21;
    public Double value22;
    public Double value23;

    public String tableName;
    public Integer orderer;
    public Integer tgId;
    public String um;
    public String _group;


    private boolean equal(Object control, Object test) {
        if(null == control) {
            return null == test;
        }
        return control.equals(test);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrognosisUrtRecord that = (PrognosisUrtRecord) o;
        return equal(_group,that._group)
                && equal(columnName,that.columnName)
                && equal(orderer,that.orderer)
                && equal(tableName,that.tableName)
                && equal(tgId,that.tgId)
                && equal(um,that.um)
                && equal(value0,that.value0)
                && equal(value1,that.value1)
                && equal(value10,that.value10)
                && equal(value11,that.value11)
                && equal(value12,that.value12)
                && equal(value13,that.value13)
                && equal(value14,that.value14)
                && equal(value15,that.value15)
                && equal(value16,that.value16)
                && equal(value17,that.value17)
                && equal(value18,that.value18)
                && equal(value19,that.value19)
                && equal(value2,that.value2)
                && equal(value20,that.value20)
                && equal(value21,that.value21)
                && equal(value22,that.value22)
                && equal(value23,that.value23)
                && equal(value3,that.value3)
                && equal(value4,that.value4)
                && equal(value5,that.value5)
                && equal(value6,that.value6)
                && equal(value7,that.value7)
                && equal(value8,that.value8)
                && equal(value9,that.value9);


        /*if (!columnName.equals(that.columnName)) return false;
        if (!orderer.equals(that.orderer)) return false;
        if (!tableName.equals(that.tableName)) return false;
        if (!tgId.equals(that.tgId)) return false;
        if (!um.equals(that.um)) return false;
        if (!value0.equals(that.value0)) return false;
        if (!value1.equals(that.value1)) return false;
        if (!value10.equals(that.value10)) return false;
        if (!value11.equals(that.value11)) return false;
        if (!value12.equals(that.value12)) return false;
        if (!value13.equals(that.value13)) return false;
        if (!value14.equals(that.value14)) return false;
        if (!value15.equals(that.value15)) return false;
        if (!value16.equals(that.value16)) return false;
        if (!value17.equals(that.value17)) return false;
        if (!value18.equals(that.value18)) return false;
        if (!value19.equals(that.value19)) return false;
        if (!value2.equals(that.value2)) return false;
        if (!value20.equals(that.value20)) return false;
        if (!value21.equals(that.value21)) return false;
        if (!value22.equals(that.value22)) return false;
        if (!value23.equals(that.value23)) return false;
        if (!value3.equals(that.value3)) return false;
        if (!value4.equals(that.value4)) return false;
        if (!value5.equals(that.value5)) return false;
        if (!value6.equals(that.value6)) return false;
        if (!value7.equals(that.value7)) return false;
        if (!value8.equals(that.value8)) return false;
        if (!value9.equals(that.value9)) return false;*/

        //return true;
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = 31 * result + value0.hashCode();
        result = 31 * result + value1.hashCode();
        result = 31 * result + value2.hashCode();
        result = 31 * result + value3.hashCode();
        result = 31 * result + value4.hashCode();
        result = 31 * result + value5.hashCode();
        result = 31 * result + value6.hashCode();
        result = 31 * result + value7.hashCode();
        result = 31 * result + value8.hashCode();
        result = 31 * result + value9.hashCode();
        result = 31 * result + value10.hashCode();
        result = 31 * result + value11.hashCode();
        result = 31 * result + value12.hashCode();
        result = 31 * result + value13.hashCode();
        result = 31 * result + value14.hashCode();
        result = 31 * result + value15.hashCode();
        result = 31 * result + value16.hashCode();
        result = 31 * result + value17.hashCode();
        result = 31 * result + value18.hashCode();
        result = 31 * result + value19.hashCode();
        result = 31 * result + value20.hashCode();
        result = 31 * result + value21.hashCode();
        result = 31 * result + value22.hashCode();
        result = 31 * result + value23.hashCode();
        result = 31 * result + tableName.hashCode();
        result = 31 * result + orderer.hashCode();
        result = 31 * result + tgId.hashCode();
        result = 31 * result + um.hashCode();
        result = 31 * result + _group.hashCode();
        return result;
    }
}
