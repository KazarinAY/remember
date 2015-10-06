YUI.add("screen.prognosisURT.aggregated_ee_heat", function (Y) {

    var Login = Y.namespace('ru.sanors.energy_portal.login'),
        PrognosisURT = Y.namespace("ru.sanors.energy_portal.prognosisURT"),
        AggregatedEeHeat = Y.namespace("ru.sanors.energy_portal.prognosisURT.aggregated_ee_heat"),
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Plugins = Y.namespace("ru.sanors.energy_portal.plugins"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app');

    AggregatedEeHeat.createDynamicContent = function () {
        AggregatedEeHeat.containerWidth = document.body.clientWidth;
        AggregatedEeHeat.containerHeight = document.body.clientHeight;
        //
        Y.one("#aggregated_ee_heat-content #aggregated_ee_heat_container").setStyle("height", AggregatedEeHeat.containerHeight-70);
        Y.one("#aggregated_ee_heat-content #aggregated_ee_heat_stub_home").setStyle("height", 180);


        //Y.one("#aggregated_ee_heat_container #aggregated_ee_heat_graf").setStyle("height", AggregatedEeHeat.containerHeight - 200);


        AggregatedEeHeat.dateTimePanel = new Y.DateTimePanel({
            hideEndDate: false,
            containerID: 'aggregated_ee_heat_dt_panel',
            startDT: null,
            btn_show: true,
            style: {
            },
            onShow: function () {
                AggregatedEeHeat.loadTable(true);
            }


        });

        AggregatedEeHeat.tabview = new Y.TabView({
            srcNode: '#aggregated_ee_heat_tabpanel'
        });
        AggregatedEeHeat.tabview.render();

        /*var tableCmp = Y.FreezedDataTable;
        if ((Y.UA.ie > 0) && (Y.UA.ie < 9)) {
            tableCmp = Y.Grid;
        }*/
        var cellFormatConsumers = function (cell) {
            if (cell.value == "Итого") return '<b>Итого</b>';
            return cell.value;

        };
        var tableCmp = Y.Grid;
        AggregatedEeHeat.Table = new tableCmp({
            fileName:"AggregatedEeHeat",
            width: "100%",
            containerName: '#aggregated_ee_heat_table',
            srcNode: '#aggregated_ee_heat_table',

            height: document.body.clientHeight - 180,
            scrollable: "xy",
            url: "services/tei/get_aggregated_ee_heat/",
            /*freezedContainerWidth: "105px",*/
            schema: {
                resultListLocator: "data[0].records"
            },
            /*freezedColumns: [{
                label: "&nbsp;", children: [
                    {className: 'btn_export_to_excel align-center',
                        key: "consumer",
                        label: 'Потребители',
                        formatter: Component.cellFormatDT,
                        resizeable: true,
                        allowHTML: true
                    }
                ]}],*/
            columns: [
                {
                    key: "consumer",
                    label: 'Потребители',
                    resizeable: true,
                    formatter: cellFormatConsumers, allowHTML: true,
                    className: 'btn_export_to_excel align-left', editable: false,
                    nodeFormatter: function (o) {
                        //row.one('td').setAttribute('rowspan', 2);
                        var consumer = o.data.consumer,
                            row = o.cell.ancestor(),
                            avg3 = parseFloat(o.data.avg_13) + parseFloat(o.data.avg_21) + parseFloat(o.data.avg_gvs),
                            avg4 = parseFloat(o.data.avg_100) +avg3,
                            sum3 = parseFloat(o.data.sum_100) + parseFloat(o.data.sum_13) + parseFloat(o.data.sum_21) + parseFloat(o.data.sum_gvs),
                            sum4 = parseFloat(o.data.sum_100) + sum3,
                            avg_tph = parseFloat(o.data.avg_100_tph)+parseFloat(o.data.avg_21_tph)+parseFloat(o.data.avg_13_tph),
                            sum_tph = parseFloat(o.data.sum_100_tph)+parseFloat(o.data.sum_21_tph)+parseFloat(o.data.sum_13_tph);

                        avg4 = Component.formatDouble(avg4);
                        sum4 = Component.formatDouble(sum4);
                        avg3 = Component.formatDouble(avg3);
                        sum3 = Component.formatDouble(sum3);
                        avg_tph = Component.formatDouble(avg_tph);
                        sum_tph = Component.formatDouble(sum_tph);

                        if (consumer == 'Итого'){
                            o.cell.addClass('align-left-bold');
                            row.insert(
                                '<tr  class="yui3-datatable-even " data-yui3-record="model_7">'+
                                    '<td class="yui3-datatable-col-consumer btn_export_to_excel align-left-bold yui3-datatable-cell ">В том числе отработанный  пар</td>'+
                                    '<td class="yui3-datatable-col-avg_100 align-right-bold yui3-datatable-cell "></td>'+
                                    '<td colspan="3" class="yui3-datatable-col-avg_13 align-center-bold yui3-datatable-cell ">'+avg3+'</td>'+

                                    '<td class="yui3-datatable-col-sum_100 align-right-bold yui3-datatable-cell "></td>'+
                                    '<td colspan="4" class="yui3-datatable-col-sum_100 align-center-bold yui3-datatable-cell ">'+sum3+'</td>'+
                                    '</tr>',
                                'after');

                            row.insert(
                                '<tr  class="yui3-datatable-even " data-yui3-record="model_7">'+
                                    '<td class="yui3-datatable-col-consumer btn_export_to_excel align-left-bold yui3-datatable-cell ">Отпуск тепла (Гкал)</td>'+
                                    '<td colspan="4" class="yui3-datatable-col-avg_100 align-center-bold yui3-datatable-cell ">'+avg4+'</td>'+
                                    '<td colspan="4" class="yui3-datatable-col-sum_100 align-center-bold yui3-datatable-cell ">'+sum4+'</td>'+
                                    '</tr>',
                                'after');
                            row.insert(
                                '<tr  class="yui3-datatable-even " data-yui3-record="model_7">'+
                                    '<td class="yui3-datatable-col-consumer btn_export_to_excel align-left-bold yui3-datatable-cell ">Отпуск пара (т/ч)</td>'+
                                    '<td colspan="4" class="yui3-datatable-col-avg_100 align-center-bold yui3-datatable-cell ">'+avg_tph+'</td>'+
                                    '<td colspan="4" class="yui3-datatable-col-sum_100 align-center-bold yui3-datatable-cell ">'+sum_tph+'</td>'+
                                    '</tr>',
                                'after');

                        }

                        o.cell.set('text', consumer);
                        return false;

                        var profit = o.data.price - o.data.cost,
                            prefix = '$',
                            row;

                        if (profit < 0) {
                            prefix = '-' + prefix;
                            profit = Math.abs(profit);
                            row = o.cell.ancestor();

                            o.cell.addClass('negative');
                            row.one('td').setAttribute('rowspan', 2);

                            row.insert(
                                '<tr class="auth"><td colspan="3">' +
                                    '<button class="ok">authorize</button>' +
                                    '<button class="stop">discontinue</button>' +
                                    '</td></tr>',
                                'after');
                        }

                        o.cell.set('text', prefix + profit.toFixed(2));
                        return false;
                    }
                },
                {label: "Средние значения", children: [
                    {key: "avg_100", label: "100<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "avg_21", label: "21<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "avg_13", label: "13<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "avg_gvs", label: "ГВС<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble}
                ]},
                {label: "Суммарные значения", children: [
                    {key: "sum_100", label: "100<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "sum_21", label: "21<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "sum_13", label: "13<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble},
                    {key: "sum_gvs", label: "ГВС<br>(Гкал)", allowHTML: true, className: 'align-right', formatter: Component.cellFormatDouble}
                ]}




            ],
            onAfterResponse: function (r) {
                Sanors.Portal.loadWindow.hide();

            },
            before: function () {

            }


        });


        AggregatedEeHeat.Table.render();

        AggregatedEeHeat.loadTable();

    };

    AggregatedEeHeat.loadTable = function (isNecessarily) {
        if ((isNecessarily == true) || (AggregatedEeHeat.bdt != AggregatedEeHeat.dateTimePanel.getBeginDate()) || (AggregatedEeHeat.edt != AggregatedEeHeat.dateTimePanel.getEndDate())) {

            AggregatedEeHeat.bdt = AggregatedEeHeat.dateTimePanel.getBeginDate();
            AggregatedEeHeat.edt = AggregatedEeHeat.dateTimePanel.getEndDate();
            Sanors.Portal.loadWindow.show();

            AggregatedEeHeat.Table.load({
                request: "?bdt=" + AggregatedEeHeat.bdt + "&edt=" + AggregatedEeHeat.edt
            });

        }


    }



}, '0.1', {
    requires: ['sanors-components', 'plugins', 'grid', 'date-time-panel', 'list-panel']
});