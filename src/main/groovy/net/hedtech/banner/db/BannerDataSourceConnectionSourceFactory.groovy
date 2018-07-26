/* *****************************************************************************
Copyright 2018 Ellucian Company L.P. and its affiliates.
****************************************************************************** */

package net.hedtech.banner.db

import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSource
import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSourceFactory
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.springframework.beans.BeansException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import javax.sql.DataSource


class BannerDataSourceConnectionSourceFactory extends DataSourceConnectionSourceFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public ConnectionSource<DataSource, DataSourceSettings> create(String name, DataSourceSettings settings) {

        String dataSourceName = "dataSource"
        try {
            DataSource springDataSource = (DataSource)this.applicationContext.getBean(dataSourceName, DataSource.class);
            return new DataSourceConnectionSource(name, springDataSource, settings);
        } catch (NoSuchBeanDefinitionException var6) {
            return super.create(name, settings);
        }
    }
}