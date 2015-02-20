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

package org.qucosa.migration;

/**
 * @author claussni
 * @date 19.02.15.
 */
public interface DestinationRepositoryProvider<T> {
    void ingest(T ingestObject) throws Exception;

    int modifyObjectState(String pid, String state) throws Exception;

    boolean hasObject(String pid) throws Exception;

    void purgeObject(String pid) throws Exception;
}
