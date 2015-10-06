YUI.add("screen.prognosisURT", function (Y) {

    var Login = Y.namespace('ru.sanors.energy_portal.login'),
        Express = Y.namespace("ru.sanors.energy_portal.express"),
        PrognosisURT = Y.namespace("ru.sanors.energy_portal.prognosisURT"),
        Component = Y.namespace("ru.sanors.energy_portal.component"),
        Plugins = Y.namespace("ru.sanors.energy_portal.plugins"),
        Sanors = Y.namespace('ru.sanors.energy_portal.app');

    PrognosisURT.createDynamicContent = function () {
        Y.one("#urt-content").setStyle("display", "block");
        //document.body.clientHeight
        Y.one("#urt-content #urt_stub_home").setStyle("height",document.body.clientHeight/2.5);







    };

    PrognosisURT.getProducts = function() {
        return Y.all('#urt-content #urt_menu_container .product');
    };
    PrognosisURT.switchMenu = function(args) {
        switch (args._currentTarget.id) {
            case 'menu_aggregated_ee_heat':
                Sanors.Portal.render().navigate(Component.context_root + '/prognosis_urt/aggregated_ee_heat',null,args);
                break;
            case 'menu_prognosis_urt_station':
                Sanors.Portal.render().navigate(Component.context_root + '/prognosis_urt/prognosis_urt_station',null,args);
                break;

            case 'urt_siteMap':
                Sanors.Portal.render().navigate(Component.context_root + '/express/',null,args);
                break;


            default :
                alert('Действие не назначено');
                //Sanors.Portal.render().navigate(Component.context_root + '/express/');
                break;

        }
    };



}, '0.1', {
    requires: ['sanors-components', 'plugins', 'grid', 'date-time-panel']
});
