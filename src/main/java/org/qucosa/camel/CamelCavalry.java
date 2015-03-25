/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.qucosa.camel.component.Opus4Repository;

/**
 * @author claussni
 * @date 23.03.15.
 */
public class CamelCavalry {

    private CamelContext ctx;

    public CamelCavalry(Opus4Repository src) throws Exception {
        setup(src);
    }

    protected void setup(Opus4Repository opus4Repository) throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("qucosaDataSource", opus4Repository);

        ctx = new DefaultCamelContext(registry);
        ctx.setStreamCaching(true);
        ctx.setAllowUseOriginalMessage(false);

        ctx.addRoutes(new QucosaStagingRoute());
    }

    public void call() {
        try {
            ctx.start();

            ProducerTemplate template = ctx.createProducerTemplate();
            template.sendBody("direct:tenantMigration", "SLUB");

            ctx.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
