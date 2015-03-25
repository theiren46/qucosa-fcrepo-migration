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
import org.qucosa.opus.Opus4ImmutableRepository;
import org.qucosa.sword.QucosaSwordDeposit;

/**
 * @author claussni
 * @date 23.03.15.
 */
public class CamelCavalry {

    private CamelContext ctx;

    public CamelCavalry(Opus4ImmutableRepository src, QucosaSwordDeposit dest) {
        setup(src, dest);
    }

    protected void setup(
            Opus4ImmutableRepository opus4ImmutableRepository,
            QucosaSwordDeposit qucosaSwordDeposit) {

        SimpleRegistry registry = new SimpleRegistry();
        registry.put("qucosaDataSource", opus4ImmutableRepository);
        registry.put("swordDeposit", qucosaSwordDeposit);

        ctx = new DefaultCamelContext(registry);
        ctx.setStreamCaching(true);
        ctx.setAllowUseOriginalMessage(false);

    }

    public void call() {
        try {
            ctx.addRoutes(new QucosaStagingRoute());
            ctx.start();

            ProducerTemplate template = ctx.createProducerTemplate();
            template.sendBody("direct:tenantMigration", "SLUB");

            ctx.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
