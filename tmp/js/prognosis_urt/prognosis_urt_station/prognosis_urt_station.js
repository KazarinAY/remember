YUI.add("screen.prognosis_urt.prognosis_urt_station", function (Y) {

    var PrognosisUrtStation = Y.namespace("ru.sanors.energy_portal.prognosis_urt.prognosis_urt_station"),
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app');

    PrognosisUrtStation.createDynamicContent = function (initialData) {
        console.log("PrognosisUrtStation.createDynamicContent()...");

        PrognosisUrtStation.containerHeight = document.body.clientHeight - 60;
        PrognosisUrtStation.tableHeight = PrognosisUrtStation.containerHeight -80;
        Y.one("#prognosis_urt_station-content").setStyle("display", "block");

        PrognosisUrtStation.containerWidth = Y.one("#prognosis_urt_station-cnt")._node.clientWidth;

        Y.one("#prognosis_urt_station-content").setStyle("display", "block");

        Y.one("#prognosis_urt_station-content #prognosis_urt_station_container").setStyle("height", PrognosisUrtStation.containerHeight);

        Y.one("#prognosis_urt_station-content #prognosis_urt_station-stub_home").setStyle("height", 80);

        Y.one("#prognosis_urt_station-content #prognosis_urt_graf_main").setStyle("height",PrognosisUrtStation.tableHeight-95 /*PrognosisUrtStation.containerHeight - 200*/);

        Y.one("#prognosis_urt_station-content #prognosis_urt_generator_table").setStyle("height",PrognosisUrtStation.tableHeight + 560);

        Y.one("#prognosis_urt_station-cnt").setStyle("height", PrognosisUrtStation.containerHeight);

        PrognosisUrtStation.cellFormatEVR = function (cell) {
            switch (cell.value) {
                case 1:
                    return "<span style='color:red'>Да</span>";
                case 0:
                    return "Нет";
                case null:
                    return "Нет";
                default:
                    return null;
            }

        };

        var editOpenType = "dblclick";
        if ((Y.Device.mobile()) || (Y.Device.tablet()) || (Y.Device.ipad())
            || (Y.Device.ipod()) || (Y.Device.iphone()) || (Y.Device.android()) || (Y.Device.androidTablet()) || (Y.Device.blackberryTablet())) {
            editOpenType = "click";
        }


        PrognosisUrtStation.dateTimePanel = new Y.DateTimePanel({
            containerID: 'prognosis_urt_station_dt_panel',
            hideEndDate: true,
            btn_show: true,
            startDT: 'tomorrow',
            onShow: function () {
                PrognosisUrtStation.loadUserTable();
            },
            onClickPlusBtn: function () {
                PrognosisUrtStation.loadUserTable();
            },
            onClickMinusBtn: function () {
                PrognosisUrtStation.loadUserTable();
            },
            onCalendarSelect: function () {
                PrognosisUrtStation.loadUserTable();
            }
        });

        PrognosisUrtStation.tabview = new Y.TabView({
            srcNode: '#prognosis_urt_panel'
        });

        PrognosisUrtStation.generatorsTabview = new Y.TabView({
            srcNode: '#prognosis_urt_generator_hour_panel'
        });

        PrognosisUrtStation.tabviewCurrentPage = 0;

        PrognosisUrtStation.generatorsTabviewCurrentPage = 0;

        PrognosisUrtStation.tabview.render();

        PrognosisUrtStation.generatorsTabview.render();

        var tableCmp = Y.FreezedDataTable,
            frizTbl = true;

        if((Y.UA.ie > 0) && (Y.UA.ie < 9)) {
            tableCmp = Y.Grid;
            frizTbl = false;
        }

        PrognosisUrtStation.stationTable = new PrognosisUrtStation.UrtDataTable({
            fileName: "PrognosisUrtStation",
            containerName: '#prognosis_urt_station_table',
            height: PrognosisUrtStation.tableHeight,
            editorConfig: {
                stopEditable: [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41, 42, 43,44]
            },
            url: "services/prognosis_urt/get_station_prognosis/",
            onRowClick: function (e) {
                var recordClass='hidden-row',target = e.target,
                    record = this.getRecord(target),
                    columnName = record._state.data.columnName.value;

                var items = this.data._items;

                for (var i = 0, len = items.length; i < len; ++i) {
                    if (items[i]._state.data._group.value === columnName) {
                        var row = this.getRow(i);
                        if (row.hasClass(recordClass)) {
                            row.removeClass('hidden-row');
                            PrognosisUrtStation.hiddenRows[columnName]=true;
                        }
                        else {
                            row.addClass('hidden-row');
                            PrognosisUrtStation.hiddenRows[columnName]=false;
                        }
                    }

                }

                return;

            },
            onAfterResponse: function (r) {
                Y.all("#prognosis_urt_station_table .yui3-datatable-data tr").setStyle("cursor", "pointer");
                Sanors.Portal.loadWindow.hide();
            }

        });

        PrognosisUrtStation.generatorsTable = new tableCmp({
            fileName: "PrognosisUrtStation",
            containerName: '#prognosis_urt_generator_table',
            srcNode: '#prognosis_urt_generator_table',
            height: PrognosisUrtStation.tableHeight + 550,
            width: "100%",
            scrollable: "xy", //todo errors on FreezedDataTable 322 line "if (bd.contains(e.target))"
            mousewheel: 320,
            editable:  true,
            editOpenType: editOpenType,
            sortable: false,
            emptyMessage: "Отсутствуют записи за указанный период",
            freezedContainerWidth:  "25%",
            freezedColumns: [
                {
                    className: 'btn_export_to_excel align-left',
                    key: "valueName",
                    label: '&nbsp;',
                    formatter: PrognosisUrtStation.generatorsCellFormatter,
                    resizeable: true,
                    allowHTML: true,
                    editable: false
                }
            ],
            url: "services/prognosis_urt/get_generator_prognosis/",
            schema: {
                resultListLocator: "data[0].records"
            },
            columns: [
                {
                    className: 'btn_export_to_excel align-left',
                    key: "valueName",
                    label: '&nbsp;',
                    formatter: PrognosisUrtStation.generatorsCellFormatter,
                    resizeable: true,
                    allowHTML: true,
                    editable: false
                },
                {label: "&nbsp;", className: 'btn_export_to_excel align-center',key: "measureUnit", formatter:PrognosisUrtStation.generatorsCellFormatter, editable: false, resizeable: true, allowHTML: true, width: "15%"},
                PrognosisUrtStation.getVariantColumn(1),
                PrognosisUrtStation.getVariantColumn(2),
                PrognosisUrtStation.getVariantColumn(3),
                PrognosisUrtStation.getVariantColumn(4),
                PrognosisUrtStation.getVariantColumn(5)
            ],
            onRowClick: PrognosisUrtStation.onRowClick ,
            onAfterResponse: PrognosisUrtStation.onAfterResponse,
            onCellEditorSave: PrognosisUrtStation.onCellEditorSave
        });

        PrognosisUrtStation.parametersTable = new Y.FreezedDataTable({
            mousewheel: 320,
            fileName: "PrognosisUrtStation",
            containerName: '#prognosis_urt_parameters_table',
            srcNode: '#prognosis_urt_parameters_table',
            freezedContainerWidth: "44px",
            width: "100%",
            height: PrognosisUrtStation.tableHeight,
            scrollable: "xy",
            url: "services/prognosis_urt/get_parameters",
            sortable: false, schema: {
                resultListLocator: "data[0].records"
            },
            freezedColumns: [
                {label: "&nbsp;", className: 'btn_export_to_excel', children: [
                        {className: 'align-center',
                            key: "hour",
                            label: 'Час',
                            formatter: Component.cellFormatHR,
                            resizeable: true,
                            allowHTML: true
                        }
                ]}
            ],
            columns: [

                {label: "&nbsp;", children: [{className: 'align-center',key: "hour",label: 'Час',formatter: PrognosisUrtStation.rowFormatter,resizeable: true,allowHTML: true}]},
                {label: "Потери конденсата", children: [
                    {key: "condensateLosses", label: "(Т/ч)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {label: "Удельный расход топлива", children: [
                    {key: "coeffBte", label: "(кг/Гкал)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {key: "boilerEfficiency", label: "КПД котла", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble},
                {key: "kTv1", label: "Коэф. Использования резерва", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble},
                {key: "kTv2", label: "Разница энтальпий ОП и ПВ", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble},
                {label: "Удельные СН на ТЭ", children: [
                    {key: "coeffSnEtl", label: "(кВтч/Гкал)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {label: "Удельные СН на ЭЭ", children: [
                    {key: "coeffSnEel", label: "(%)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {label: "РОУ 140/100", children: [
                    {key: "rou100", label: "(Т/ч)", allowHTML: true, className: 'align-left', editor: "select", formatter: PrognosisUrtStation.cellFormatEVR,
                        editorConfig: {
                            selectOptions: { "Нет": 'Нет', "Да": 'Да'}
                        }}
                ]},
                {label: "РОУ 140/21", children: [
                    {key: "rou21", label: "(Т/ч)", allowHTML: true, className: 'align-left', editor: "select", formatter: PrognosisUrtStation.cellFormatEVR,
                        editorConfig: {
                            selectOptions: { "Нет": 'Нет', "Да": 'Да'}
                        }}
                ]},
                {label: "РОУ 140/13", children: [
                    {key: "rou13", label: "(Т/ч)", allowHTML: true, className: 'align-left', editor: "select", formatter: PrognosisUrtStation.cellFormatEVR,
                        editorConfig: {
                            selectOptions: { "Нет": 'Нет', "Да": 'Да'}
                        }}
                ]},
                {label: "Калорийность", children: [
                    {key: "caloricContent", label: "(ккалл/кг)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {label: "Вусл", children: [
                    {key: "bUslKindling", label: "растопка", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]},
                {label: "Ь тэ нр пр", children: [
                    {key: "bTeNrPr", label: "(гр/кВтч)", allowHTML: true, className: 'align-left', editor: "number", formatter: Component.cellFormatDouble}
                ]}


            ], onAfterResponse: function (e) {
                Sanors.Portal.loadWindow.hide();
                for (var i = 0; i < 24; i++) {
                    var row = PrognosisUrtStation.parametersTable.table.getRow(i);
                    if (PrognosisUrtStation.selectedParametersTableRows["row_" + i]) {
                        ros.addClass('selected-lilac-bold');
                    }
                }
            },
            onCellEditorSave: function (e) {

                var record = PrognosisUrtStation.getParametersRecord(e.record._state.data, e.colKey, e.newVal);
                Y.io("services/prognosis_urt/set_parameters/", {
                    method: "POST",
                    data: Y.JSON.stringify(record),
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    on: {
                        success: function (x, o) {
                            PrognosisUrtStation.loadUserTable();
                        }, failure: function () {
                            PrognosisUrtStation.loadUserTable();
                        }
                    }, arguments: {
                        failure: Sanors.Portal.IOarguments.HideMessage
                    }
                });
            }, editable: true, defaultEditor: 'number', editOpenType: editOpenType,
            onRowClick: function(e) {
                var target = e.target,
                    record = this.getRecord(target),
                    valueName = record._state.data.hour.value,
                    table = PrognosisUrtStation.parametersTable.table,
                    items = table.data._items;
                for (var i = 0; i < items.length; ++i) {
                    var tmp = items[i]._state.data.hour.value;
                    if (tmp == valueName) {
                        var row = table.getRow(i);
                        if (row.hasClass('selected-lilac-bold')) {
                            row.removeClass('selected-lilac-bold');
                            PrognosisUrtStation.selectedParametersTableRows["row_" + valueName] = false;
                        }
                        else {
                            row.addClass('selected-lilac-bold');
                            PrognosisUrtStation.selectedParametersTableRows["row_" + valueName] = true;
                        }
                    }
                }
                return;
            }
        });


        bindUI();

        PrognosisUrtStation.loadUserTable();

    };

    var bindUI = function () {
        console.log("bindUI()...");
        PrognosisUrtStation.tabview.after('selectionChange', function (e) {
            var index = 0;
            try {
                index = e.newVal.get('index');
            } catch (e) {
                return;
            }
            PrognosisUrtStation.tabviewCurrentPage = index;
            PrognosisUrtStation.loadUserTable();
        });

        PrognosisUrtStation.generatorsTabview.after('selectionChange', function (e) {
            var index = 0;
            try {
                index = e.newVal.get('index');
            } catch (e) {
                return;
            }
            PrognosisUrtStation.generatorsTabviewCurrentPage = index;
            PrognosisUrtStation.loadUserTable();
        });

        Y.one('#prognosis_urt_station_copy_btn').on('click', function (e) {
            PrognosisUrtStation.copyDayWindow = PrognosisUrtStation.getCopyDayWindow();
            PrognosisUrtStation.copyDayWindow.show();
        });

        Y.one('#prognosis_urt_station_copy_hour_btn').on('click', function (e) {
            PrognosisUrtStation.postRequestCopyHour();
        });

    }

    PrognosisUrtStation.loadUserTable = function () {
        var generatorType =  0;
        if (PrognosisUrtStation.tabviewCurrentPage == 0) {
            PrognosisUrtStation.showMain(generatorType);
        }else if (PrognosisUrtStation.tabviewCurrentPage == 1) {
            Sanors.Portal.loadWindow.show();
            PrognosisUrtStation.stationTable.load({
                request: "?dt=" + PrognosisUrtStation.dateTimePanel.getBeginDate() + "&type="+generatorType
            });
            PrognosisUrtStation.stationTable.render();
        } else if (PrognosisUrtStation.tabviewCurrentPage == 2) {
            PrognosisUrtStation.generatorsTable.render();
            Sanors.Portal.loadWindow.show();
            PrognosisUrtStation.generatorsTable.load({
                request: "?dt=" + PrognosisUrtStation.dateTimePanel.getBeginDate() + "&hour=" + PrognosisUrtStation.getGeneratorHour()
            });
        } else if (PrognosisUrtStation.tabviewCurrentPage == 3) {
            Sanors.Portal.loadWindow.show();
            PrognosisUrtStation.parametersTable.load({
                request: "?bdt=" + PrognosisUrtStation.dateTimePanel.getBeginDate() + "&edt=" + PrognosisUrtStation.dateTimePanel.getBeginDate() + "&type="+generatorType
            });
            PrognosisUrtStation.parametersTable.render();
        }
    };

    PrognosisUrtStation.rowFormatter = function (cell) {
        var row = "row_" + cell.data.hour;
        if (PrognosisUrtStation.selectedParametersTableRows[row]) {
            cell.rowClass="selected-lilac-bold";
        }
        if (cell.value == 99) return '<b>Итого</b>';
        return cell.value;
    }

    PrognosisUrtStation.selectedParametersTableRows = {};

    PrognosisUrtStation.onRowClick = function (e) {
        var target = e.target,
            record = this.getRecord(target),
            valueName = record._state.data.valueName.value,
            table = PrognosisUrtStation.generatorsTable.table,
            ftable = PrognosisUrtStation.generatorsTable.ftable,
            items = table.data._items;

        for (var i = 0; i < items.length; ++i) {
            var tmp = items[i]._state.data.tgGroup.value;

            if (tmp == valueName) {
                var row = table.getRow(i);
                var frow = ftable.getRow(i);
                if (row.hasClass('hidden-row')) {
                    row.removeClass('hidden-row');
                    frow.removeClass('hidden-row');
                }
                else {
                    row.addClass('hidden-row');
                    frow.addClass('hidden-row');
                }
            }
        }
        return;
    }

    PrognosisUrtStation.getRecord = function (data, colKey, newVal) {
        var tableRecord = {},
            value = (newVal == null || newVal == '') ? '' : '' + newVal;
        if (value.indexOf('-+') != -1 || value.indexOf('--') != -1 || value.indexOf('++') != -1 || (value.indexOf('-') === 0 && value.length === 1)) {
            alert("Некорректное значение");
            PrognosisUrtStation.loadUserTable;
            return null;
        }
        tableRecord.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        tableRecord.hour = colKey.substring(5);
        tableRecord.columnName = (data.columnName.value=='Противодавление'||data.columnName.value=='Pнто')?'производственный отбор':data.columnName.value;
        tableRecord.tableName = data.tableNgeneratorame.value;
        tableRecord.isEndDateUpdate = (value == '') ? false : (value.substring(0, 1) == '+' ? true : false);
        tableRecord.tgId = data.tgId.value == null ? null : data.tgId.value;
        tableRecord.value = (value == '') ? null : value.replace('+', '').replace(',','.');
        tableRecord.urtType = 0;

        return tableRecord;
    };

    PrognosisUrtStation.onAfterResponse = function (r) {
        try {
            var responseArray = PrognosisUrtStation.listColumnsToArray(r.response.results),
                valuesLength = 5,
                valuesResponse;
            for (var n = 1; n <= valuesLength; n++){
                if (responseArray[0]['variant_' + n] == null) {
                    valuesResponse = n - 1;
                    break;
                }
            }
            PrognosisUrtStation.generatorsTable.schema = {resultListLocator: "responseArray"};
            PrognosisUrtStation.generatorsTable.table.set('data', responseArray);

            PrognosisUrtStation.addColumns(valuesLength, valuesResponse);
        } catch(e) {
            /*NON*/
        }
        Sanors.Portal.loadWindow.hide();
    };

    PrognosisUrtStation.listColumnsToArray = function (data) {

        var myJson = [  {},{},{},{},{},{},{},{},{},{},
            {},{},{},{},{},{},{},{},{},{},
            {},{},{},{},{},{},{},{},{},{},
            {},{},{},{},{},{},{},{},{},{},
            {},{},{},{},{},{},{},{},{},{}   ];
        for (var index = 0; index < data.length; index++) {
            (myJson[index])['orderer'] = data[index].orderer;
            (myJson[index])['valueName'] = data[index].valueName;
            (myJson[index])['measureUnit'] = data[index].measureUnit;
            (myJson[index])['tgGroup'] = data[index].tgGroup;
            for (var j = 0; j <  data[index].values.length; j++) {
                (myJson[index])['variant_'+ (j+1)] = data[index].values[j];
            }
        }
        return myJson;
    };

    PrognosisUrtStation.addColumns = function (valuesLength, valuesResponse) {
        var withMinus = true, withoutMinus = false, withPlus = true, withoutPlus = false;

        for (var i = 1; i <= valuesLength; i++) {
            if (i > 1 && i == valuesResponse && valuesResponse < valuesLength) {
                PrognosisUrtStation.generatorsTable.table.modifyColumn('variant_' + i, PrognosisUrtStation.getVariantColumn(i, withPlus, withMinus));
                Y.one('.prognosis_urt_generator_plus' + i).on('click', function (e) {
                    PrognosisUrtStation.postRequestAddVariant(e)
                });
                Y.one('.prognosis_urt_generator_minus' + i).on('click', function (e) {
                    PrognosisUrtStation.postRequestRemoveVariant(e);
                });
            } else if (i == 1 && valuesResponse == 1) {
                PrognosisUrtStation.generatorsTable.table.modifyColumn('variant_' + i, PrognosisUrtStation.getVariantColumn(i, withPlus, withoutMinus));
                Y.one('.prognosis_urt_generator_plus' + i).on('click', function (e) {
                    PrognosisUrtStation.postRequestAddVariant(e)
                });
            } else if (i > 1 && i == valuesResponse && valuesResponse == valuesLength) {
                PrognosisUrtStation.generatorsTable.table.modifyColumn('variant_' + i, PrognosisUrtStation.getVariantColumn(i, withoutPlus, withMinus));
                Y.one('.prognosis_urt_generator_plus' + i).on('click', function (e) {
                    PrognosisUrtStation.postRequestAddVariant(e)
                });
            } else if (i <= valuesResponse && valuesResponse <= valuesLength) {
                PrognosisUrtStation.generatorsTable.table.modifyColumn('variant_' + i, PrognosisUrtStation.getVariantColumn(i, withoutPlus, withoutMinus));
            }
            /* else {
             PrognosisUrtStation.generatorsTable.table.modifyColumn('variant_'+i,{label: '' + i + " вариант"});
             }*/
        }
    };

    PrognosisUrtStation.onCellEditorSave = function (e) {
        var record = PrognosisUrtStation.getCellRecord(e.record._state.data, e.colKey, e.newVal);
        if (record != null) {
            Y.io("services/prognosis_urt/set_generator_prognosis/", { //todo kill new
                method: "POST",
                data: Y.JSON.stringify(record),
                headers: {
                    'Content-Type': 'application/json'
                },
                on: {
                    success: function (x, o) {
                        PrognosisUrtStation.loadUserTable(true);
                    }, failure: function () {
                        PrognosisUrtStation.loadUserTable();
                    }
                }, arguments: {
                    failure: Sanors.Portal.IOarguments.HideMessage
                }
            });
        } else {
            PrognosisUrtStation.loadUserTable();
        }
    };

    PrognosisUrtStation.getCellRecord = function (data, colKey, newVal) {
        console.log("getCellRecord tableRecord:")
        var tableRecord = {},
            value = (newVal == null || newVal == '') ? '' : '' + newVal;
        if (value.indexOf('-+') != -1 || value.indexOf('--') != -1 || value.indexOf('++') != -1 || (value.indexOf('-') === 0 && value.length === 1)) {
            alert("Некорректное значение");
            PrognosisUrtStation.loadUserTable;
            return null;
        }
        tableRecord.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        tableRecord.hour = PrognosisUrtStation.getGeneratorHour();
        tableRecord.valueName = data.valueName.value.substring(0,data.valueName.value.length - 4);
        tableRecord.tgId = PrognosisUrtStation.getTgId(data);
        tableRecord.value = (value == '') ? null : value.replace('+', '').replace(',','.');
        var variant = colKey;
        tableRecord.variant = "prognosis" + ( (variant == "variant_1") ? '' : variant.substring(8) );
        console.log(tableRecord);
        return tableRecord;
    };

    /**
     * Копирует текущий час в остальные часы до конца дня.
     * @param e
     */
    PrognosisUrtStation.postRequestCopyHour = function () {
        var record = {};
        record.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        record.hour = PrognosisUrtStation.getGeneratorHour(); //0-23
        record.urtType = "prognosis";

        Y.io("services/prognosis_urt/copy_hour/", {
            method: "POST",
            data: Y.JSON.stringify(record),
            headers: {
                'Content-Type': 'application/json'
            },
            on: {
                success: function (x, o) {
                    //console.log("postRequestCopyHour success");
                }, failure: function () {
                    //console.log("postRequestCopyHour failure");
                }
            }, arguments: {
                failure: Sanors.Portal.IOarguments.HideMessage
            }
        });

    };

    PrognosisUrtStation.postRequestAddVariant = function (e) {
        console.log("postRequestAddVariant() record:")
        var record = {};
        record.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        record.hour = PrognosisUrtStation.getGeneratorHour();
        record.variantNumber = parseInt(e._currentTarget.className.charAt(28));
        console.log(record);

        Y.io("services/prognosis_urt/add_variant/", {
            method: "POST",
            data: Y.JSON.stringify(record),
            headers: {
                'Content-Type': 'application/json'
            },
            on: {
                success: function (x, o) {
                    PrognosisUrtStation.loadUserTable(true);
                }, failure: function () {
                    PrognosisUrtStation.loadUserTable();
                }
            }, arguments: {
                failure: Sanors.Portal.IOarguments.HideMessage
            }
        });
    };

    PrognosisUrtStation.postRequestRemoveVariant = function (e) {
        console.log("postRequestRemoveVariant() record:")
        var record = {};
        record.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        record.hour = PrognosisUrtStation.getGeneratorHour();
        record.variantNumber = parseInt(e._currentTarget.className.charAt(29));
        console.log(record);

        Y.io("services/prognosis_urt/remove_variant/", {
            method: "POST",
            data: Y.JSON.stringify(record),
            headers: {
                'Content-Type': 'application/json'
            },
            on: {
                success: function (x, o) {
                    PrognosisUrtStation.loadUserTable(true);
                }, failure: function () {
                    PrognosisUrtStation.loadUserTable();
                }
            }, arguments: {
                failure: Sanors.Portal.IOarguments.HideMessage
            }
        });
    };

    PrognosisUrtStation.getVariantColumn = function(prognosisNumb, isLastColumn, isNotFirstColumn) {

        var plus = "";
        if (isLastColumn) {
            //plus = " <button class='prognosis_urt_generator_plus" + prognosisNumb +"'>+</button>";
            plus = "<b><span class='prognosis_urt_generator_plus"+ prognosisNumb +" button_blue'>+</span></b>";
        }
        var minus = "";
        if (isNotFirstColumn) {
            //minus = " <button class='prognosis_urt_generator_minus" + prognosisNumb +"'>-</button>";
            minus = "<b><span class='prognosis_urt_generator_minus"+ prognosisNumb +" button_blue'>-</span></b>";
        }

        return {
            label: '' + prognosisNumb + " вариант &nbsp;&nbsp;&nbsp;" + minus + "&nbsp;&nbsp;&nbsp;" + plus,
            className: 'align-center',
            key: 'variant_' + prognosisNumb,
            formatter: PrognosisUrtStation.cellFormatByRow,
            allowHTML: true,
            editOpenType: "dblclick",
            editable: true,
            editor: "urtNumber",
            editorConfig: { stopEditable: [0, 4,5,6,7, 11,12,13,14, 18,19,20,21, 25,26,27,28] }
        }
    };

    PrognosisUrtStation.cellFormatByRow = function (cell) {
        try {
            if ((cell.value === null ) || (cell.value === ""))
                return null;

            var stringValue = "" + cell.value,
                parsedValue = parseFloat(cell.value);

            if (stringValue.substring(0, 1) === '+')
                return stringValue

            if (stringValue.indexOf('-+') != -1 || stringValue.indexOf('--') != -1 || stringValue.indexOf('++') != -1 || (stringValue.indexOf('-') === 0 && stringValue.length === 1)) {
                return stringValue;
            }

            if (cell.value.toFixed(0) != cell.value)
                return (cell.value.toFixed(2));

            return (cell.value.toFixed(0));
        } catch (e) {
            return (cell.value);
        }
    };

    PrognosisUrtStation.hideContent = function () {

    };

    PrognosisUrtStation.showContent = function () {
        PrognosisUrtStation.stationTable.render();
        PrognosisUrtStation.generatorsTable.render();
        PrognosisUrtStation.parametersTable.render();
    };

    PrognosisUrtStation.getTgId = function (data) {
        var tgTmp = data.valueName.value.charAt(data.valueName.value.length - 1);
        if (tgTmp < 4)
            return parseInt(tgTmp) + 1;
        else if (tgTmp == 5)
            return 5;
        else if (tgTmp == 6)
            return 176;
        else if (tgTmp >= 7)
            return parseInt(tgTmp) - 1;
    };

    PrognosisUrtStation.getParametersRecord = function (data, colKey, newVal) {
        var tableRecord = {};

        tableRecord.dt = PrognosisUrtStation.dateTimePanel.getBeginDate();
        tableRecord.hour = data.hour.value == null ? null : data.hour.value;
        tableRecord.condensateLosses = data.condensateLosses.value == null ? null : data.condensateLosses.value;
        tableRecord.coeffBte = data.coeffBte.value == null ? null : data.coeffBte.value;
        tableRecord.boilerEfficiency = data.boilerEfficiency.value == null ? null : data.boilerEfficiency.value;
        tableRecord.kTv1 = data.kTv1.value == null ? null : data.kTv1.value;
        tableRecord.kTv2 = data.kTv2.value == null ? null : data.kTv2.value;
        tableRecord.coeffSnEtl = data.coeffSnEtl.value == null ? null : data.coeffSnEtl.value;
        tableRecord.coeffSnEel = data.coeffSnEel.value == null ? null : data.coeffSnEel.value;
        tableRecord.rou100 = (data.rou100.value == null || data.rou100.value == "Нет" || data.rou100.value == 0) ? 0 : 1;
        tableRecord.rou21 = (data.rou21.value == null || data.rou21.value == "Нет" || data.rou21.value == 0) ? 0 : 1;
        tableRecord.rou13 = (data.rou13.value == null || data.rou13.value == "Нет" || data.rou13.value == 0) ? 0 : 1;
        tableRecord.caloricContent = data.caloricContent.value == null ? null : data.caloricContent.value;
        tableRecord.bUslKindling = data.bUslKindling.value == null ? null : data.bUslKindling.value;
        tableRecord.bTeNrPr = data.bTeNrPr.value == null ? null : data.bTeNrPr.value;

        tableRecord.column = colKey;

        tableRecord.calculateType = 0;

        return tableRecord;
    };

    PrognosisUrtStation.generatorsCellFormatter = function (cell) {

        if( ( cell.data.tgGroup) && (cell.data.tgGroup != 'нагрузка') ){
            cell.rowClass="hidden-row";
        } else if(cell.data.tgGroup && cell.data.tgGroup == 'нагрузка'){
            cell.value = "<b>"+cell.value+"</b>";
            cell.rowClass="selected-lilac-bold";
        }
        return cell.value
    };

    PrognosisUrtStation.getGeneratorNumber = function () {
        switch (PrognosisUrtStation.generatorsTabviewCurrentPage) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 5;
            case 4:
                return 6;
            case 5:
                return 7;
            case 6:
                return 8;
        }
    };

    //1-24
    PrognosisUrtStation.getGeneratorHour = function () {
        return PrognosisUrtStation.generatorsTabviewCurrentPage;
    };

}, '0.1', {
    requires: ['sanors-components', 'plugins', 'grid', 'date-time-panel', 'DTEditorOptions.urtNumber', 'urt_data_table','generators_data_table','screen.prognosis_urt.prognosis_urt_graph','screen.prognosis_urt.prognosis_urt_station.copy_day_window']
});
