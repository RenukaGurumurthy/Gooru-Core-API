package org.ednovo.gooru.domain.cassandra.service;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.cassandra.model.ResourceCio;
import org.ednovo.gooru.core.constant.ColumnFamilyConstant;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDaoHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScollectionCassandraServiceImpl extends ApiCrudEntityCassandraServiceImpl<Collection, ResourceCio> implements ScollectionCassandraService , ConstantProperties {

        @Autowired
        private CollectionDaoHibernate collectionRepository;


        @Override
        String getDaoName() {
         return ColumnFamilyConstant.RESOURCE;
        }

        @Override
        protected Collection fetchSource(String gooruOid) {
                return this.getCollectionRepository().getCollectionByType(gooruOid, COLLECTION_TYPES);
        }


        public CollectionDaoHibernate getCollectionRepository() {
                return collectionRepository;
        }


}
