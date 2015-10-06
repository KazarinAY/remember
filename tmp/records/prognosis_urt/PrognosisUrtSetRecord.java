package ru.sanors.energy_portal.sc.web_portal.prognosis_urt;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: zybinra
 * Date: 26.11.14
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class PrognosisUrtSetRecord implements Serializable {
    public String dt;

    public Integer hour;

    public String valueName;

    public Double value;

    public Integer tgId;

    public String variant;

}
