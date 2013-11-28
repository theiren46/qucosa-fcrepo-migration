/*
 * Copyright (C) 2013 SLUB Dresden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.fcrepo.migration;

import fedora.fedoraSystemDef.foxml.DigitalObjectDocument;
import fedora.fedoraSystemDef.foxml.DigitalObjectType;
import fedora.fedoraSystemDef.foxml.ObjectPropertiesType;
import fedora.fedoraSystemDef.foxml.PropertyType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static java.lang.System.exit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            DigitalObjectDocument dof = DigitalObjectDocument.Factory.newInstance();

            DigitalObjectDocument.DigitalObject dobj = dof.addNewDigitalObject();
            dobj.setVERSION(DigitalObjectType.VERSION.X_1_1);

            ObjectPropertiesType pt = dobj.addNewObjectProperties();

            {
                PropertyType p = pt.addNewProperty();
                p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
                p.setVALUE("Active");
            }
            {
                PropertyType p = pt.addNewProperty();
                p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
                p.setVALUE("Synthetic Demo Object");
            }
            {
                PropertyType p = pt.addNewProperty();
                p.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNER_ID);
                p.setVALUE("SLUB");
            }

            dof.save(System.out, new XmlOptions().setSavePrettyPrint());
        } catch (Exception e) {
            log.error(e.getMessage());
            exit(1);
        }
    }


    private static Configuration getConfiguration() throws ConfigurationException {
        return new SystemConfiguration();
    }

}
