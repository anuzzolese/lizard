<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
    <groupId>${groupId}</groupId>
	<artifactId>${artifactId}</artifactId>
    <version>${versionId}</version>
	
	<build>
    	<plugins>
      		<plugin>
        		<groupId>org.apache.maven.plugins</groupId>
        		<artifactId>maven-surefire-plugin</artifactId>
        		<version>2.19</version> <!-- see surefire-page for available versions -->
        		<configuration>
          			<systemPropertyVariables>
            			<maven.home>${"$"}{maven.home}</maven.home>
          			</systemPropertyVariables>
        		</configuration>
      		</plugin>
      		<plugin>
        		<groupId>org.apache.maven.plugins</groupId>
        		<artifactId>maven-compiler-plugin</artifactId>
        		<version>3.5.1</version>
        		<configuration>
          			<source>1.8</source>
          			<target>1.8</target>
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
    	
		<!-- Apache Jena -->
		<dependency>
    		<groupId>org.apache.jena</groupId>
    		<artifactId>apache-jena-libs</artifactId>
    		<type>pom</type>
    		<version>3.0.0</version>
  		</dependency>
  		
		
		<!-- Freemarker -->
		<dependency>
			<groupId>org.freemarker</groupId>
			<artifactId>freemarker</artifactId>
			<version>2.3.20</version>
		</dependency>
		
		<!-- Jersey -->
		<dependency>
    		<groupId>org.glassfish.jersey.containers</groupId>
    		<artifactId>jersey-container-servlet</artifactId>
    		<version>2.23.2</version>
    		<scope>provided</scope>
		</dependency>
		
		<dependency>
			<groupId>org.osgi</groupId>
			<artifactId>org.osgi.service.component.annotations</artifactId>
			<version>1.3.0</version>
		</dependency>

		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-log4j12</artifactId>
			<version>1.7.7</version>
		</dependency>
		
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-jdk14</artifactId>
			<version>1.7.7</version>
		</dependency>

	</dependencies>
</project>