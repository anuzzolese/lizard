<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<parent>
		<groupId>eu.mario-project</groupId>
		<artifactId>parent</artifactId>
		<version>0.1-SNAPSHOT</version>
		<relativePath>../parent</relativePath>
	</parent>

	<modelVersion>4.0.0</modelVersion>
	<groupId>${groupId}</groupId>
	<artifactId>${artifactId}</artifactId>
	<version>0.9-SNAPSHOT</version>
	<packaging>bundle</packaging>
	<!-- name>MARIO OntNet</name -->

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.felix</groupId>
				<artifactId>maven-bundle-plugin</artifactId>
				<extensions>true</extensions>
				<configuration>
					<instructions>
					</instructions>
				</configuration>
			</plugin>
		</plugins>
	</build>

	<dependencies>

		<!-- Lizard Commons -->
		<dependency>
			<groupId>it.cnr.istc.stlab</groupId>
			<artifactId>lizard.commons</artifactId>
			<version>0.9-SNAPSHOT</version>
		</dependency>

		<dependency>
			<groupId>org.osgi</groupId>
			<artifactId>org.osgi.core</artifactId>
		</dependency>
		<dependency>
			<groupId>org.osgi</groupId>
			<artifactId>osgi.cmpn</artifactId>
		</dependency>
		<dependency>
			<groupId>org.osgi</groupId>
			<artifactId>org.osgi.service.metatype.annotations</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.felix</groupId>
			<artifactId>org.apache.felix.scr.ds-annotations</artifactId>
		</dependency>

		<dependency>
			<groupId>javax.ws.rs</groupId>
			<artifactId>javax.ws.rs-api</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.jena</groupId>
			<artifactId>jena-osgi</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.slf4j</groupId>
					<artifactId>slf4j-api</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.slf4j</groupId>
					<artifactId>slf4j-log4j12</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.slf4j</groupId>
					<artifactId>jcl-over-slf4j</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.osgi</groupId>
					<artifactId>org.osgi.core</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>eu.mario-project</groupId>
			<artifactId>eu.mario-project.commons.web.base</artifactId>
			<version>0.1-SNAPSHOT</version>
		</dependency>

		<!-- Swagger annotations -->
		<dependency>
			<groupId>io.swagger</groupId>
			<artifactId>swagger-annotations</artifactId>
			<version>1.5.0</version>
		</dependency>

		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
		</dependency>

	</dependencies>
</project>