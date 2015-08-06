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

package org.qucosa.migration.processors.transformations;

import com.xmlns.foaf.x01.PersonDocument;
import de.slubDresden.InfoDocument;
import de.slubDresden.InfoType;
import de.slubDresden.SubmitterType;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.Document;
import noNamespace.OpusDocument;
import noNamespace.Person;

public class PersonInfoProcessor extends MappingProcessor {
    @Override
    public void process(OpusDocument opusDocument, ModsDocument modsDocument, InfoDocument infoDocument) throws Exception {
        Document opus = opusDocument.getOpus().getOpusDocument();
        InfoType info = infoDocument.getInfo();

        mapPersonSubmitter(opus, info);
    }

    private void mapPersonSubmitter(Document opus, InfoType info) {
        for (Person submitter : opus.getPersonSubmitterArray()) {
            final String name = combineName(submitter.getFirstName(), submitter.getLastName());
            final String phone = submitter.getPhone();
            final String mbox = submitter.getEmail();

            SubmitterType st = (SubmitterType)
                    select("slub:submitter[foaf:Person/foaf:name='" + name + "']", info);

            if (st == null) {
                st = info.addNewSubmitter();
                PersonDocument.Person foafPerson = st.addNewPerson();
                foafPerson.setName(name);
                if (phone != null && !phone.isEmpty()) foafPerson.setPhone(phone);
                if (mbox != null && !mbox.isEmpty()) foafPerson.setMbox(mbox);
                signalChanges("SLUB-INFO");
            }
        }
    }

    private String combineName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName).append(' ');
        }
        sb.append(lastName);
        return sb.toString();
    }
}
