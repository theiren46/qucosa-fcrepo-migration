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

    private final Opus4ImmutableRepository srcRepo;
    private final QucosaSwordDeposit destRepo;

    public CamelCavalry(Opus4ImmutableRepository src, QucosaSwordDeposit dest) {
        this.srcRepo = src;
        this.destRepo = dest;
    }

    public void call() {
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("qucosaDataSource", srcRepo);
        registry.put("swordDeposit", destRepo);

        DefaultCamelContext ctx = new DefaultCamelContext(registry);
        ctx.setStreamCaching(true);

        try {
            ctx.addRoutes(new QucosaStagingRoute());
            ctx.start();

            ProducerTemplate template = ctx.createProducerTemplate();
            template.sendBody("direct:qucosa-resources", "SLUB");

            ctx.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
