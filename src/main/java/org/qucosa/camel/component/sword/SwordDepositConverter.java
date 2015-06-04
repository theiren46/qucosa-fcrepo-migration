package org.qucosa.camel.component.sword;

import gov.loc.mets.MetsDocument;
import org.apache.camel.Converter;

@Converter
public class SwordDepositConverter {

    @Converter
    public SwordDeposit toSwordDeposit(MetsDocument metsDocument) {
        return new SwordDeposit(metsDocument);
    }

}
