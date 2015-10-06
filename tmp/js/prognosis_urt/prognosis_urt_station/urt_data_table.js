YUI.add('urt_data_table', function (Y) {
    var Y = Y,
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app'),
        PrognosisUrtStation = Y.namespace("ru.sanors.energy_portal.prognosis_urt.prognosis_urt_station"),
        Initialization = Y.namespace("ru.sanors.energy_portal.initialization");


    var getOpenType = function () {

        if ((Y.Device.mobile()) || (Y.Device.tablet()) || (Y.Device.ipad())
            || (Y.Device.ipod()) || (Y.Device.iphone()) || (Y.Device.android()) || (Y.Device.androidTablet()) || (Y.Device.blackberryTablet())) {
            return  "click";

        }
        return "dblclick";
    }

    var getEditable = function () {
        var initialData = Initialization.getCommonData();

        for (var i = 0; i < initialData.CURRENT_USER.permission.length; i++) {
            var p = initialData.CURRENT_USER.permission[i].name;
            if (p == "edit_urt_prognosis") {
                return true;
            }
        }
        return false;
    }

    PrognosisUrtStation.paramFormatter = function (cell) {

        cell.rowClass = (cell.value.substr(0, 11) == "нагрузка_тг") ? "hidden-row" : "";

        if((cell.data._group)&&(cell.data._group!=='base') && (!PrognosisUrtStation.hiddenRows[cell.data._group])){
            cell.rowClass="hidden-row";

        } else if(cell.data._group=='base'){
            cell.value = "<b>"+cell.value+"</b>";
            cell.rowClass="selected-lilac-bold";
            //PrognosisUrtStation.hiddenRows[cell.data.columnName]=true;
        }

        return cell.value
    }
    PrognosisUrtStation.hiddenRows={}



    var cellFormatByRow = function (cell) {
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


    UrtDataTable.NAME = "urt_data_table";
    UrtDataTable.ATTRS = {
        schema: {
            resultListLocator: "data[0].records"

        },
        width: "100%",
        scrollable: "xy",
        mousewheel: 320,
        editable: false,
        defaultEditor: 'number', editOpenType: getOpenType(),
        sortable: false,
        emptyMessage: "Отсутствуют записи за указанный период",
        editorConfig: null,
        columns: [
            {
                className: 'btn_export_to_excel align-left',
                key: "columnName",
                label: '&nbsp;',
                formatter: PrognosisUrtStation.paramFormatter,
                resizeable: true,
                allowHTML: true,
                editable: false

            },
            {key: "um", label: "&nbsp;", allowHTML: true, className: 'align-center', formatter:  Component.cellFormatHR, editable: false},
            {key: "value0", label: "1", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value1", label: "2", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value2", label: "3", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value3", label: "4", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value4", label: "5", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value5", label: "6", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value6", label: "7", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value7", label: "8", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value8", label: "9", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value9", label: "10", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value10", label: "11", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value11", label: "12", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value12", label: "13", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value13", label: "14", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value14", label: "15", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value15", label: "16", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value16", label: "17", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value17", label: "18", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value18", label: "19", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value19", label: "20", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value20", label: "21", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value21", label: "22", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value22", label: "23", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null},
            {key: "value23", label: "24", allowHTML: true, className: 'align-right', formatter: cellFormatByRow, editor: "urtNumber", editorConfig: null}
        ],
        onAfterResponse: function (r) {
            Sanors.Portal.loadWindow.hide();
        }
    }

    function UrtDataTable(config) {
        config.editable = getEditable(),

        config = Y.merge(UrtDataTable.ATTRS, config);
        for (i = 0; i < config.columns.length; i++) {
            if (config.columns[i].editor === "urtNumber") {
                config.columns[i].editorConfig = config.editorConfig
            }
        }
        UrtDataTable.superclass.constructor.apply(this, arguments);
    }


    Y.extend(UrtDataTable, Y.Grid,
        {
            initializer: function (config) {

                this.render()

            }

        });

    PrognosisUrtStation.UrtDataTable = UrtDataTable;

}, '0.1', {
    requires: [
        "grid", "DTEditorOptions.urtNumber"
    ]
});
