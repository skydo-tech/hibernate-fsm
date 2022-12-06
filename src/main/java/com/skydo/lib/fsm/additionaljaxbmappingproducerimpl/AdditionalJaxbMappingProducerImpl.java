package com.skydo.lib.fsm.additionaljaxbmappingproducerimpl;

import com.skydo.lib.fsm.servicecontributor.FSMService;
import org.hibernate.boot.jaxb.internal.MappingBinder;
import org.hibernate.boot.model.source.internal.hbm.MappingDocument;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.ServiceRegistry;
import org.jboss.jandex.IndexView;

import java.util.ArrayList;
import java.util.Collection;

public class AdditionalJaxbMappingProducerImpl implements AdditionalJaxbMappingProducer {

    @Override
    public Collection<MappingDocument> produceAdditionalMappings(MetadataImplementor metadata, IndexView jandexIndex, MappingBinder mappingBinder, MetadataBuildingContext buildingContext) {
        MetadataBuildingOptions metadataBuildingOptions = metadata.getMetadataBuildingOptions();
        final ServiceRegistry serviceRegistry = metadataBuildingOptions.getServiceRegistry();
        final FSMService fsmService = serviceRegistry.getService( FSMService.class );

        final ArrayList<MappingDocument> additionalMappingDocuments = new ArrayList<>();

        fsmService.initialize(metadata);

        return additionalMappingDocuments;
    }
}