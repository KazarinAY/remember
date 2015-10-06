YUI.add('screen.prognosis_urt.prognosis_urt_station.copy_day_window', function (Y) {
    var PrognosisUrtStation = Y.namespace("ru.sanors.energy_portal.prognosis_urt.prognosis_urt_station"),
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app');


    PrognosisUrtStation.getCopyDayWindow = function (initialData){
        if (PrognosisUrtStation.copyDayWindow){
            setDateFromData();
            return PrognosisUrtStation.copyDayWindow;
        }  else
            return createCopyDayWindow(initialData);


    }

    var setDateFromData = function(){
        Y.one("#prognosis_urt_station_copy_panel_from").setHTML(PrognosisUrtStation.dateTimePanel.getBeginDate() + "&nbsp;" + PrognosisUrtStation.generatorType.get('value'))
    };

    var createCopyDayWindow= function(initialData){

        var copyDayWindow  = createPanel();
        copyDayWindow.render();

        PrognosisUrtStation.copyFromDateTime = new Y.DateTimePanel({
            containerID: 'prognosis_urt_station_dt_copy_panel',
            hideEndDate: true,
            btn_show: false,
            startDT: null
        });
        setDateFromData();
        return  copyDayWindow

    }

    var createPanel = function(permission){
        var srcNode = "#prognosis_urt_station-copy_dialog",
            calTmpl ='<strong>Скопировать данные из:</strong><div style="color:red; text-align:center" id="prognosis_urt_station_copy_panel_from"></div><strong>В:</strong><div id="prognosis_urt_station_copy_panel_to"></div>' +
                '<div id="prognosis_urt_station_dt_copy_panel" style="text-align:center" class="yui_cal"></div>'+
                '<div id="prognosis_urt_station_copy_panel_day_type" style="text-align:center"><label><input type="radio" checked value="0" name="prognosis_urt_station_copy_panel_day_type_group">Прогноз</label><br/><label><input type="radio" value="1" name="prognosis_urt_station_copy_panel_day_type_group">Мин/Макс</label></div>'

        var p = new Y.Panel({
            srcNode: srcNode,
            bodyContent: '<div class="message icon-warn" style="display:table; width:100%">'+calTmpl+'</div>',
            width: '330px',
            height: 300,
            zIndex: 99999,
            centered: true,
            modal: true,
            visible: false,
            buttons: [
                {
                    value: 'Копировать',
                    section: Y.WidgetStdMod.FOOTER,
                    action: function (e) {
                        e.halt(true);
                        saveData.call(this);
                    }
                },
                {
                    value: 'Выход',
                    section: Y.WidgetStdMod.FOOTER,
                    action: function (e) {
                        e.halt(true);
                        this.hide();
                    }
                }
            ]
        });
        return p;

    }

    var saveData = function(){
        var me = this,
            type = Y.one("#prognosis_urt_station_copy_panel_day_type  input[name=prognosis_urt_station_copy_panel_day_type_group]:checked").get("value");




        var url = Y.Lang.sub("services/prognosis_urt/copy_day_data/{to_dt}/{from_dt}/{type}/", {
            to_dt :PrognosisUrtStation.copyFromDateTime.getBeginDate(),
            from_dt :PrognosisUrtStation.dateTimePanel.getBeginDate(),
            type  : type
        });


        Y.io(url, {
            method: "GET",
            headers: { 'Content-Type': 'application/json'},
            on: {
                success: function (x, o) {

                    try {
                        var messages = Y.JSON.parse(o.responseText),
                            record = messages.data[0];


                        PrognosisUrtStation.generatorType.set('value', (type.toLowerCase() == '0') ? "Прогноз" : "Мин/Макс")
                        PrognosisUrtStation.dateTimePanel.setDates(PrognosisUrtStation.copyFromDateTime.getBeginDate(), PrognosisUrtStation.dateTimePanel.getBeginDate())
                        PrognosisUrtStation.loadUserTable();
                    } finally {
                        me.hide();
                    }


                }
            },
            arguments: {
                failure: Sanors.Portal.IOarguments.HideMessage
            }
        });

    }




}, '0.1', {
    requires: ['sanors-components', 'plugins', 'grid', 'date-time-panel']
});
