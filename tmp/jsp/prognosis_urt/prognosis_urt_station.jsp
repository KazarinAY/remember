<%@ page contentType="text/html;charset=windows-1251" language="java" %>
<div id="prognosis_urt_station-content" style="display: none; width: 100% ">
    <div id="prognosis_urt_station-stub_home" class="yui3-u align-stub-home"
         style="width: 1px;max-width:960px"></div>
    <div id="prognosis_urt_station_container"
         style="margin:0px auto; width: 98%; height: 200px; min-width:960px; border: 1px solid rgb(186, 186, 186);">
        <div id="prognosis_urt_station-copy_dialog"></div>
        <div id="prognosis_urt_station-cnt" class="example yui3-skin-sam">
            <div id="" style="padding-left:10px; padding-top:10px;" class="yui3-skin-sam"></div>
            <div style="display:table; margin-left: 0.5em; clear: both; width: 100%;">
                <div id="prognosis_urt_station_dt_panel" style="display: table-cell; width: 1%; min-width: 730px"
                     class="yui3-skin-sam"></div>
                <div style="display: table-cell;width: 25%;vertical-align: top;">
                    <div style="margin-right: 1em; text-align: left;">
                        <input type="button" value="Скопировать день" style="font-size: 25px; height: 40px;"
                               id="prognosis_urt_station_copy_btn">
                    </div>
                </div>
            </div>

            <div style="clear: left"></div>

            <div id="prognosis_urt_panel">
                <ul>
                    <li><a href="#tb_prognosis_urt_graf_main">Главная</a></li>
                    <li><a href="#tb_prognosis_urt_station">Станция</a></li>
                    <li><a href="#tb_prognosis_urt_generators">Генераторы</a></li>
                    <li><a href="#tb_prognosis_urt_parameters">Параметры</a></li>
                </ul>
                <div>
                    <div id="tb_prognosis_urt_graf_main">
                        <div id="prognosis_urt_graf_main" style="position:relative;" class="yui3-skin-sam"></div>
                        <div id="prognosis_urt_graf_tablo" style="position:relative;width: 95%;text-align: center"
                             class="yui3-skin-sam">
                            <table border="1" class="over_tbl_border" style="width:60%;margin: 0 auto;">
                                <tr>
                                    <td colspan="8" style="background-color: #3F51B5; color: #ffffff;font-size: 12px">
                                        <b><span id="prognosis_urt_graf_tablo_dt"></span></b></td>
                                </tr>
                                <tr>
                                    <td valign="center" style="text-align: center; font-size: 12px">Выработка ЭЭ</td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Отпуск с шин</td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Отпуск Тепла</td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Расход условного
                                        топлива
                                    </td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Расход натурального
                                        топлива
                                    </td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Калорийность</td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Удельный расход
                                        топлива на ЭЭ
                                    </td>
                                    <td valign="center" style="text-align: center; font-size: 12px">Удельный расход
                                        топлива на ТЭ
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="center" style="text-align: center; font-size: 10px">Мвт</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">Мвт</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">Гкал</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">тут</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">тм3</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">ккалл/кг</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">г/кВтч</td>
                                    <td valign="center" style="text-align: center; font-size: 10px">кг/Гкал</td>
                                </tr>
                                <tr>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_power"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_down_tires"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_heat_supply"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_rut"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_rnt"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_caloric_content"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_urt_ee"></span></b></td>
                                    <td valign="top" style="text-align: center; font-size: 200%; white-space:nowrap"><b><span
                                            id="prognosis_urt_graf_tablo_urt_te"></span></b></td>
                                </tr>

                            </table>
                        </div>
                        <!--div id="prognosis_urt_graf_main" style="position:relative; float:left; width: 100%;height:98%;padding-top: 10px "></div-->
                    </div>
                    <div id="tb_prognosis_urt_station">
                        <div id="prognosis_urt_station_table" class="yui3-skin-sam tableDemo"></div>
                    </div>

                    <div id="tb_prognosis_urt_generator">
                        <div style="display: table-cell;width: 25%;vertical-align: top;">
                            <div style="margin-right: 1em; text-align: left;">
                                <input type="button" value="Скопировать в остальные часы" style="font-size: 25px; height: 40px;"
                                       id="prognosis_urt_station_copy_hour_btn">
                            </div>
                        </div>
                        <div id="prognosis_urt_generator_hour_panel">
                            <ul>
                                <li><a href="#tb_prognosis_urt_generator_hour_1">1</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_2">2</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_3">3</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_4">4</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_5">5</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_6">6</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_7">7</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_8">8</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_9">9</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_10">10</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_11">11</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_12">12</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_13">13</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_14">14</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_15">15</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_16">16</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_17">17</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_18">18</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_19">19</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_20">20</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_21">21</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_22">22</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_23">23</a></li>
                                <li><a href="#tb_prognosis_urt_generator_hour_24">24</a></li>
                            </ul>
                            <div>
                                <div id="tb_prognosis_urt_generator_hour_1">
                                    <div id="prognosis_urt_generator_hour_1_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_2">
                                    <div id="prognosis_urt_generator_hour_2_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_3">
                                    <div id="prognosis_urt_generator_hour_3_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_4">
                                    <div id="prognosis_urt_generator_hour_4_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_5">
                                    <div id="prognosis_urt_generator_hour_5_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_6">
                                    <div id="prognosis_urt_generator_hour_6_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_7">
                                    <div id="prognosis_urt_generator_hour_7_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_8">
                                    <div id="prognosis_urt_generator_hour_8_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_9">
                                    <div id="prognosis_urt_generator_hour_9_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_10">
                                    <div id="prognosis_urt_generator_hour_10_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_11">
                                    <div id="prognosis_urt_generator_hour_11_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_12">
                                    <div id="prognosis_urt_generator_hour_12_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_13">
                                    <div id="prognosis_urt_generator_hour_13_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_14">
                                    <div id="prognosis_urt_generator_hour_14_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_15">
                                    <div id="prognosis_urt_generator_hour_15_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_16">
                                    <div id="prognosis_urt_generator_hour_16_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_17">
                                    <div id="prognosis_urt_generator_hour_17_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_18">
                                    <div id="prognosis_urt_generator_hour_18_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_19">
                                    <div id="prognosis_urt_generator_hour_19_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_20">
                                    <div id="prognosis_urt_generator_hour_20_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_21">
                                    <div id="prognosis_urt_generator_hour_21_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_22">
                                    <div id="prognosis_urt_generator_hour_22_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_23">
                                    <div id="prognosis_urt_generator_hour_23_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                                <div id="tb_prognosis_urt_generator_hour_24">
                                    <div id="prognosis_urt_generator_hour_24_table"
                                         class="yui3-skin-sam tableDemo"></div>
                                </div>
                            </div>
                        </div>
                        <div id="prognosis_urt_generator_table" class="yui3-skin-sam tableDemo"></div>
                    </div>
                    <div id="tb_prognosis_urt_parameters">
                        <div id="prognosis_urt_parameters_table" class="yui3-skin-sam tableDemo"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>