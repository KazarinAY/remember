package ru.sanors.energy_portal.sc.web_portal.prognosis_urt;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: zybinra
 * Date: 26.11.14
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class PrognosisUrtParametersRecord implements Serializable {
    public String dt;

    public String column;

    public Integer hour;

    public Double condensateLosses;

    public Double coeffBte;

    public Double boilerEfficiency;

    public Double kTv1;

    public Double kTv2;

    public Double coeffSnEtl;

    public Double coeffSnEel;

    public Integer rou100;

    public Integer rou21;

    public Integer rou13;

    public Double caloricContent;

    public Integer calculateType;

    public Double bUslKindling;

    public Double bTeNrPr;

}
