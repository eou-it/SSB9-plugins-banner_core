/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
dataSource {
	pooled = true
	driverClassName = "oracle.jdbc.OracleDriver"
    dialect = "org.hibernate.dialect.Oracle10gDialect"
	username = "banproxy"
	password = "u_pick_it"
}
hibernate {
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			url = "jdbc:oracle:thin:@AP-BGLR-LP0111.internal.sungard.corp:1521:BAN83"
		}
	}
	test {
		dataSource {
			url = "jdbc:oracle:thin:@AP-BGLR-LP0111.internal.sungard.corp:1521:BAN83"
		}
	}
	production {
		dataSource {
			url = "jdbc:oracle:thin:@AP-BGLR-LP0111.internal.sungard.corp:1521:BAN83"
		}
	}
}