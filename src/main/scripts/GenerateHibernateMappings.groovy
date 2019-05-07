/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import groovy.io.FileType
import groovy.text.SimpleTemplateEngine

includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << new File("${grailsHome}/scripts/Bootstrap.groovy")

/**
 * Grails target for Hinernate hibernate.cfg.xm file mappings.
 * Usage:  'grails generate-hibernate-mappings'
 * */

target(main: "Generates Hibernate hibernate.cfg.xml file mappings") {
    //depends(parseArguments, bootstrap)
    //getClass().classLoader.rootLoader?.addURL(new File(classesDirPath).toURL())

    println "Starting generating Hibernate hibernate.cfg.xml file mappings..."

    final hibernateTemplate = '''<?xml version='1.0' encoding='utf-8'?>
   <!-- *****************************************************************************
    Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
    ****************************************************************************** -->

    <!--
     Hibernate configuration file. All mapped classes must be identified within this
     configuration file.  Note: Annotations within the class files are used in lieu
     of additional mapping files.  JPA is not used, hence while annotations are leveraged
     there is no concept of an 'entity manager' within this project.
     -->
   <!DOCTYPE hibernate-configuration PUBLIC
           "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
           "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

   <hibernate-configuration>
       <session-factory>
       <property name="dialect">org.hibernate.dialect.Oracle10gDialect</property>
   <% domainsList.each{ domainClass ->
     out << """     <mapping class="${domainClass}"/>
   """}%>
   </session-factory>
   </hibernate-configuration>
   '''

    def appName = grails.util.Metadata.current.'app.name'
    def map = ["$appName": "$basedir"];
    def dataSourceEntries = [];

    new File("$basedir/plugins").eachDir() { dir ->
        def pluginName = dir.name.minus(".git").replaceAll("_", "-")
        map."$pluginName" = dir.getPath()
    }

    map.each {

        def dir = new File(it.value + "/src/groovy")
        if (dir.exists()) {


            def hbFileName = "hibernate-" + it.key + ".cfg.xml"


            def jpaDomains = []
            dir.eachFileRecurse(FileType.FILES) { file ->
                file.eachLine {
                        // condition to detect an @Entity annotation
                    ln ->
                        if (ln =~ '^@Entity') {
                            def content = file.text
                            GroovyClassLoader gcl = new GroovyClassLoader();
                            def d = ((GroovyObject) gcl.parseClass(content).newInstance())

                            jpaDomains << d.getClass().getName()
                        }
                }
            }

            if (!jpaDomains.empty) {
                final engine = new SimpleTemplateEngine()
                final thingsTemplateEngine = engine.createTemplate(hibernateTemplate)

                def output = thingsTemplateEngine.make(domainsList: jpaDomains.toArray()).toString()

                def hibernateFolder = new File("${it.value}${File.separator}grails-app${File.separator}conf${File.separator}hibernate")
                if (!hibernateFolder.exists()) {
                    hibernateFolder.mkdirs()
                }

                def outputFile = new File("${it.value}${File.separator}grails-app${File.separator}conf${File.separator}hibernate${File.separator}${hbFileName}")

                def fileWriter = new FileWriter(outputFile)
                fileWriter.write(output.toString())
                fileWriter.close()

                dataSourceEntries << "\"classpath:${hbFileName}\""
                println "${it.key}: Generated ${hbFileName}"
            } else {
                println "${it.key}: No Domains found"
            }
        } else { println "${it.key}: No src/groovy folder found" }// if src/groovy not found
    } // each
    if (!dataSourceEntries.isEmpty()) {
        println "Add or replace config.location entry in hibernate section in DataSource.groovy of the App"
        println "config.location = ${dataSourceEntries}"
    }
    println "The END"
}
setDefaultTarget "main"