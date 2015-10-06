YUI.add("screen.prognosis_urt.prognosis_urt_graph", function (Y) {

    var Login = Y.namespace('ru.sanors.energy_portal.login'),
        Express = Y.namespace("ru.sanors.energy_portal.express"),
        PrognosisUrtStation = Y.namespace("ru.sanors.energy_portal.prognosis_urt.prognosis_urt_station"),
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Plugins = Y.namespace("ru.sanors.energy_portal.plugins"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app');

    PrognosisUrtStation.showMain = function (generatorType) {
        Sanors.Portal.loadWindow.show();
        Y.io("services/prognosis_urt/get_station_prognosis?dt=" + PrognosisUrtStation.dateTimePanel.getBeginDate() + "&type="+generatorType,
            {
                method: 'GET',
                headers: { 'Content-Type': 'application/json'},
                on: {
                    success: function (x, o) {

                        var result = "";

                        try {
                            var response = Y.JSON.parse(o.responseText),
                                resultsFirst = response.data[0].records;


                            result = o.responseText;

                        } catch (e) {
                            result = "Произошла ошибка получения данных";
                            return;
                        }


                        try {
                            if (resultsFirst) {
                                var dataGrahFirst = PrognosisUrtStation.getDataObj(resultsFirst);
                                dataGrahFirst.renderTo = 'prognosis_urt_graf_main';
                                dataGrahFirst.title = "" + PrognosisUrtStation.dateTimePanel.getBeginDate();
                                dataGrahFirst.subtitle = "";
                                Y.one("#prognosis_urt_graf_tablo_power").setHTML((dataGrahFirst.nSum).toFixed(0));
                                Y.one("#prognosis_urt_graf_tablo_down_tires").setHTML((dataGrahFirst.downTiresSum).toFixed(2));
                                Y.one("#prognosis_urt_graf_tablo_heat_supply").setHTML((dataGrahFirst.heatSum).toFixed(0));
                                Y.one("#prognosis_urt_graf_tablo_rut").setHTML((dataGrahFirst.bUslSum).toFixed(0));
                                Y.one("#prognosis_urt_graf_tablo_rnt").setHTML((dataGrahFirst.rntSum).toFixed(0));
                                Y.one("#prognosis_urt_graf_tablo_caloric_content").setHTML((dataGrahFirst.caloricContentSum/24).toFixed(0));
                                Y.one("#prognosis_urt_graf_tablo_urt_ee").setHTML(((dataGrahFirst.bUslSum - dataGrahFirst.heatSum * dataGrahFirst.bte/24/1000)*1000/dataGrahFirst.downTiresSum).toFixed(2));//(dataGrahFirst.bUslSum - dataGrahFirst.heatSum *dataGrahFirst.bte /24/1000) *1000/dataGrahFirst.downTiresSum
                                Y.one("#prognosis_urt_graf_tablo_urt_te").setHTML((dataGrahFirst.bteNrPrSum/24).toFixed(2));
                                PrognosisUrtStation.chartFirst = new Highcharts.Chart(PrognosisUrtStation.getChartConfigMain(dataGrahFirst));
                                PrognosisUrtStation.chartFirst.redraw();
                            }
                        } catch (e) {
                        }
                        Sanors.Portal.loadWindow.hide();
                    },

                    failure: function (x, o) {

                        try {
                            Sanors.Portal.loadWindow.hide();
                        } catch (e) {
                        }

                    }
                },
                arguments: {
                    failure: Sanors.Portal.IOarguments.HideMessage
                }
            });
    }

    PrognosisUrtStation.switchHour = function (res, i) {
        switch (i) {
            case(0):
                return   res.value0 == null ? null : parseFloat(res.value0.toFixed(2))
            case(1):
                return   res.value1 == null ? null : parseFloat(res.value1.toFixed(2))
            case(2):
                return   res.value2 == null ? null : parseFloat(res.value2.toFixed(2))
            case(3):
                return   res.value3 == null ? null : parseFloat(res.value3.toFixed(2))
            case(4):
                return   res.value4 == null ? null : parseFloat(res.value4.toFixed(2))
            case(5):
                return   res.value5 == null ? null : parseFloat(res.value5.toFixed(2))
            case(6):
                return   res.value6 == null ? null : parseFloat(res.value6.toFixed(2))
            case(7):
                return   res.value7 == null ? null : parseFloat(res.value7.toFixed(2))
            case(8):
                return   res.value8 == null ? null : parseFloat(res.value8.toFixed(2))
            case(9):
                return   res.value9 == null ? null : parseFloat(res.value9.toFixed(2))
            case(10):
                return   res.value10 == null ? null : parseFloat(res.value10.toFixed(2))
            case(11):
                return   res.value11 == null ? null : parseFloat(res.value11.toFixed(2))
            case(12):
                return   res.value12 == null ? null : parseFloat(res.value12.toFixed(2))
            case(13):
                return   res.value13 == null ? null : parseFloat(res.value13.toFixed(2))
            case(14):
                return   res.value14 == null ? null : parseFloat(res.value14.toFixed(2))
            case(15):
                return   res.value15 == null ? null : parseFloat(res.value15.toFixed(2))
            case(16):
                return   res.value16 == null ? null : parseFloat(res.value16.toFixed(2))
            case(17):
                return   res.value17 == null ? null : parseFloat(res.value17.toFixed(2))
            case(18):
                return   res.value18 == null ? null : parseFloat(res.value18.toFixed(2))
            case(19):
                return   res.value19 == null ? null : parseFloat(res.value19.toFixed(2))
            case(20):
                return   res.value20 == null ? null : parseFloat(res.value20.toFixed(2))
            case(21):
                return   res.value21 == null ? null : parseFloat(res.value21.toFixed(2))
            case(22):
                return   res.value22 == null ? null : parseFloat(res.value22.toFixed(2))
            case(23):
                return   res.value23 == null ? null : parseFloat(res.value23.toFixed(2))
        }
    }

    PrognosisUrtStation.getDataObj = function (res) {
        var l = res.length,


            results = {
                bee: [],
                beeR: [],
                n: [],
                hour: [],
                sn: [],
                heat: [],
                tg: [],
                nSum: 0,
                downTiresSum: 0,
                heatSum: 0,
                beeSum: 0,
                bUslSum: 0 ,
                rntSum: 0,
                caloricContentSum: 0,
                bteNrPrSum: 0,
                bte:0
            }
        var tg1,tg2,tg3,tg5,tg6,tg7,tg8;
        for (var j = 0; j < 24; j++) {
            for (var i = 0; i < l; i++) {
                try {
                    if (res[i].columnName == 'нагрузка_тг1') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg1 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг2') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg2 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг3') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg3 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг5') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg5 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг6') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg6 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг7') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg7 = true;
                        }
                    }
                    if (res[i].columnName == 'нагрузка_тг8') {
                        if(PrognosisUrtStation.switchHour(res[i], j)!= null){
                            tg8 = true;
                        }
                    }
                    if (res[i].columnName == 'Ьээ') {
                        results.beeSum += PrognosisUrtStation.switchHour(res[i], j)
                        results.bee[j] = PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Нагрузка') {
                        results.nSum += PrognosisUrtStation.switchHour(res[i], j);
                        results.n[j] = PrognosisUrtStation.switchHour(res[i], j);
                    }
                    if (res[i].columnName == 'Ь тэ нр пр') {
                        results.bteNrPrSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'СН') {
                        results.sn[j] = PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Отпуск тепла') {
                        results.heat[j] = PrognosisUrtStation.switchHour(res[i], j)
                        results.heatSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Отпуск с шин') {
                        results.downTiresSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Вусл') {
                        results.bUslSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Расход натурального топлива') {
                        results.rntSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Калорийность') {
                        results.caloricContentSum += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'Ьээ р') {
                        results.beeR[j] = PrognosisUrtStation.switchHour(res[i], j)
                    }
                    if (res[i].columnName == 'bтэ') {
                        results.bte += PrognosisUrtStation.switchHour(res[i], j)
                    }
                    results.hour[j] = j + 1;
                } catch (e) {
                    Sanors.Portal.loadWindow.hide();
                }
            }
        }
        if(tg1){
            results.tg.push('ТГ1')
        }
        if(tg2){
            results.tg.push('ТГ2')
        }
        if(tg3){
            results.tg.push('ТГ3')
        }
        if(tg5){
            results.tg.push('ТГ5')
        }
        if(tg6){
            results.tg.push('ТГ6')
        }
        if(tg7){
            results.tg.push('ТГ7')
        }
        if(tg8){
            results.tg.push('ТГ8')
        }

        return results;
    }

    PrognosisUrtStation.redraw = function (renderer,tg) {
        var top = 10;
        for(var i = 0;i<tg.length;i++){
            renderer.label(tg[i], 10, top+(i*40), null, null, null, true, null, null)
                //label: function (str, x, y, shape, anchorX, anchorY, useHTML, baseline, className)
                .attr({
                    fill: '#3F51B5',
                    stroke: 'white',
                    'stroke-width': 2,
                    padding: 5,
                    r: 5
                })
                .css({
                    color: 'white'
                })
                .add()
                .shadow(true);
        }
    }

    PrognosisUrtStation.getChartConfigMain = function (obj) {
        var me = this,
            hour = obj.hour,
            bee = obj.bee,
            n = obj.n,
            heat = obj.heat,
            sn = obj.sn,
            beeR = obj.beeR;

        var beeMin = Math.min.apply(null,obj.bee)*0.9,
            nMin = Math.min.apply(null,obj.n)*0.9,
            heatMin = Math.min.apply(null,obj.heat)*0.9,
            snMin= Math.min.apply(null,obj.sn)*0.9;

        var beeMax = Math.max.apply(null,obj.bee)*1.1,
            nMax = Math.max.apply(null,obj.n)*1.1,
            heatMax = Math.max.apply(null,obj.heat)*1.1,
            snMax= Math.max.apply(null,obj.sn)*1.1;


        var getVisibleLine = function (lineNum) {
            var r = true;
            try {
                r = PrognosisUrtStation.chartFirst.series[lineNum].visible;
            } catch (e) {
                r = true;
            }

            return r;
        }

        var minTickInterval = 1;

        return {
            chart: {
                renderTo: obj.renderTo,
                type: 'line',
                marginLeft: 150,
                zoomType: 'xy',
                events: {
                    redraw: function () {
                        me.redraw(this.renderer,obj.tg);
                    }
                }
            },
            title: {
                text: obj.title,
                x: -20
            },
            subtitle: {
                text: obj.subtitle,
                x: -20
            },
            xAxis: {
                categories: hour,
                minTickInterval: minTickInterval,
                labels: {
                    formatter: function () {
                        return this.value
                    }
                }


            },

            yAxis: [
                {
                    plotLines: [
                        {
                            value: 0,
                            width: 1,
                            color: '#808080'
                        }
                    ],
                    min: beeMin,
                    max: beeMax,//beeMin,

                    labels: {
                        formatter: function () {
                            return this.value;
                        },
                        style: {
                            color: '#89A54E'
                        }
                    },
                    title: {
                        text: 'Удельные (гр/кВТч)',
                        style: {
                            color: '#89A54E'
                        }
                    },
                    opposite: false
                },
                {
                    plotLines: [
                        {
                            value: 1,
                            width: 1,
                            color: '#4572A7'
                        }
                    ],
                    min: nMin,
                    max: nMax,

                    labels: {
                        formatter: function () {
                            return this.value;
                        },
                        style: {
                            color: '#4572A7'
                        }
                    },
                    title: {
                        text: 'Нагрузка (МВтч)',
                        style: {
                            color: '#4572A7'
                        }
                    },
                    opposite: false
                },
                {
                    plotLines: [
                        {
                            value: 2,
                            width: 1,
                            color: 'red'
                        }
                    ],
                    min: heatMin,
                    max: heatMax,

                    labels: {
                        formatter: function () {
                            return this.value;
                        },
                        style: {
                            color: 'red'
                        }
                    },
                    title: {
                        text: 'Отпус тепла (Гкал/ч)',
                        style: {
                            color: 'red'
                        }
                    },
                    opposite: true
                },
                {
                    plotLines: [
                        {
                            value: 3,
                            width: 1,
                            color: '#E65100'
                        }
                    ],
                    min: snMin,
                    max: snMax,

                    labels: {
                        formatter: function () {
                            return this.value;
                        },
                        style: {
                            color: '#E65100'
                        }
                    },
                    title: {
                        text: 'Собственные нужды (МВтч)',
                        style: {
                            color: '#E65100'
                        }
                    },
                    opposite: true
                }
            ],

            tooltip: {
                shared: true,
                formatter: function () {

                    var p = '',
                        valueSuffix = '';

                    p += '<b>Час ' + this.x + '</b><br/><br/>';
                    for (var i = 0; i < this.points.length; i++) {
                        p += '<b style="color:' + this.points[i].series.color + '">' + this.points[i].series.name + '</b>  ' + this.points[i].y + valueSuffix + '<br/>';

                    }
                    return p;
                }
            },


            legend: {
                align: 'center',
                verticalAlign: 'top',
                x: 0,
                y: 30,
                borderWidth: 0
            },
            credits: {
                enabled: false
            },
            series: [
                {
                    visible: getVisibleLine(0),
                    lineWidth: 2,
                    color: '#89A54E',
                    marker: {
                        enabled: false
                    },
                    states: {
                        hover: {
                            lineWidth: 3,
                            marker: {
                                radius: 2
                            }
                        }
                    },
                    yAxis: 0,
                    name: 'Удельные (гр/кВТч)',
                    data: bee
                },
                {
                    visible: getVisibleLine(1),
                    lineWidth: 2,
                    color: 'purple',
                    marker: {
                        enabled: false
                    },
                    states: {
                        hover: {
                            lineWidth: 3,
                            marker: {
                                radius: 2
                            }
                        }
                    },
                    yAxis: 0,
                    name: 'Удельные Р (гр/кВТч)',
                    data: beeR
                },
                {
                    visible: getVisibleLine(2),
                    lineWidth: 2,
                    color: '#4572A7',
                    marker: {
                        enabled: false
                    },
                    states: {
                        hover: {
                            lineWidth: 3,
                            marker: {
                                radius: 2
                            }
                        }
                    },
                    yAxis: 1,
                    name: 'Нагрузка (МВтч)',
                    data: n
                },
                {
                    visible: getVisibleLine(3),
                    lineWidth: 2,
                    color: 'red',
                    marker: {
                        enabled: false
                    },
                    states: {
                        hover: {
                            lineWidth: 3,
                            marker: {
                                radius: 2
                            }
                        }
                    },
                    yAxis: 2,
                    name: 'Отпуск тепла (Гкал/ч)',
                    data: heat
                },
                {
                    visible: getVisibleLine(4),
                    lineWidth: 2,
                    color: '#E65100',
                    marker: {
                        enabled: false
                    },
                    states: {
                        hover: {
                            lineWidth: 3,
                            marker: {
                                radius: 2
                            }
                        }
                    },
                    yAxis: 3,
                    name: 'Собственные нужды (МВтч)',
                    data: sn
                }
            ]

        }
    };

}, '0.1', {
    requires: []
});
