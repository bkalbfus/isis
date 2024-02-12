package org.apache.causeway.viewer.graphql.model.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.GraphQLObjectType;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.Parent;

public class GqlvTopLevelQuery
        extends GqlvAbstractCustom
        implements Parent {

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

//    private final GqlvScenario scenario;

    public GqlvTopLevelQuery(final Context context) {
        super("Query", context);

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(GqlvDomainObject.of(objectSpec, context));

                    break;
            }
        });

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo -> {
                                GqlvDomainService gqlvDomainService = GqlvDomainService.of(objectSpec, servicePojo, context);
                                addChildField(gqlvDomainService.getField());
                                domainServices.add(gqlvDomainService);
                            });
                    break;
            }
        });

        // add domain object lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addChildField(domainObject.getLookupField());
        }

//        scenario = new GqlvScenario(context);
//        addChildField(scenario.getField());

        buildObjectType();
    }

    @Override
    public GraphQLObjectType getGqlObjectType() {
        return super.getGqlObjectType();
    }

    public void addDataFetchers() {

        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetchers(this);
            }
        });


        domainObjects.forEach(domainObject -> domainObject.addDataFetchers(this));

//        scenario.addDataFetchers(this);
    }
}
