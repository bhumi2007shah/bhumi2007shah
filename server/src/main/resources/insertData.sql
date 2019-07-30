INSERT INTO COUNTRY (COUNTRY_NAME, COUNTRY_CODE, MAX_MOBILE_LENGTH, COUNTRY_SHORT_CODE)
 VALUES
 ('Australia','+61', 9,'au'),
 ('Canada','+1', 10,'ca'),
 ('India','+91', 10,'in'),
 ('Singapore','+65', 8,'sg'),
 ('United Kingdom','+44', 10,'gb'),
 ('United States','+1', 10,'us');

INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
 ( 'function','BPO'),
 ( 'function','Human Resources (HR)'),
 ( 'function','Information Technology (IT)'),
 ( 'function','Manufacturing'),
 ( 'function','Office Administration'),
 ( 'function','Sales'),

 ( 'education','BA'),
 ( 'education','BAMS'),
 ( 'education','BArch'),
 ( 'education','BBA'),
 ( 'education','BBM'),
 ( 'education','BBS'),
 ( 'education','BCA'),
 ( 'education','BCCA'),
 ( 'education','BCM'),
 ( 'education','BCom'),
 ( 'education','BCS'),
 ( 'education','BDes'),
 ( 'education','BDS'),
 ( 'education','BE'),
 ( 'education','BEd'),
 ( 'education','BFA'),
 ( 'education','BFT'),
 ( 'education','BHM'),
 ( 'education','BHMS'),
 ( 'education','BIT'),
 ( 'education','BMM'),
 ( 'education','BMS'),
 ( 'education','BPharma'),
 ( 'education','BSc'),
 ( 'education','BSSE'),
 ( 'education','BTech'),
 ( 'education','BVSc'),
 ( 'education','Commercial Art'),
 ( 'education','Diploma'),
 ( 'education','Diploma in Graphics & Animation'),
 ( 'education','Fashion/Designing'),
 ( 'education','GDBA'),
 ( 'education','HSC'),
 ( 'education','LLB'),
 ( 'education','MBBS'),
 ( 'education','Vocational Training'),
 ( 'education','ACCA'),
 ( 'education','CA'),
 ( 'education','CA IPCC'),
 ( 'education','CFA'),
 ( 'education','CS'),
 ( 'education','CWA'),
 ( 'education','EMBA'),
 ( 'education','FCA'),
 ( 'education','I PG Course'),
 ( 'education','ICAI/CMA'),
 ( 'education','ICWA'),
 ( 'education','Journalism/Mass Comm'),
 ( 'education','LLM'),
 ( 'education','MA'),
 ( 'education','MArch'),
 ( 'education','MBA'),
 ( 'education','MBA/PGDM'),
 ( 'education','MBM'),
 ( 'education','MCA'),
 ( 'education','MCM'),
 ( 'education','MCom'),
 ( 'education','MDBA'),
 ( 'education','MDP'),
 ( 'education','ME'),
 ( 'education','MEd'),
 ( 'education','MEP'),
 ( 'education','MFA'),
 ( 'education','MFC'),
 ( 'education','MFM'),
 ( 'education','MMS'),
 ( 'education','MPharma'),
 ( 'education','MPhil'),
 ( 'education','MPM'),
 ( 'education','MS'),
 ( 'education','MS CS'),
 ( 'education','MS in Industrial Psychology'),
 ( 'education','MS/MD'),
 ( 'education','MSc'),
 ( 'education','MSW'),
 ( 'education','MTech'),
 ( 'education','MVSc'),
 ( 'education','Other'),
 ( 'education','PG Diploma'),
 ( 'education','PGCBM'),
 ( 'education','PGCHRM'),
 ( 'education','PGDAC'),
 ( 'education','PGDBA'),
 ( 'education','PGDCA'),
 ( 'education','PGM'),
 ( 'education','PGP'),
 ( 'education','PGPBM'), 
 ( 'education','PHD'),
 ( 'education','PR/Advertising'),
 ( 'education','Tourism'),

 ( 'questionType','Checkbox'),
 ( 'questionType','Radio button'),
 ( 'questionType','InputBox'),
 ( 'questionType','Location'),
 ( 'questionType','Slider'),
 ( 'questionType','Calendar'),

 ( 'addressType','Job Location'),
 ( 'addressType','Interview Location'),
 ( 'addressType','Both'),

 ( 'importanceLevel','Low'),
 ( 'importanceLevel','Mid'),
 ( 'importanceLevel','High'),

 ( 'stage','Source'),
 ( 'stage','Screen'),
 ( 'stage','Interview'),
 ( 'stage','Offer'),
 ( 'stage','Reject'),

 ( 'process','Hiring Manager'),
 ( 'process','Lead Recruiter'),
 ( 'process','Sourcing'),
 ( 'process','Screening'),
 ( 'process','Interview'),
 ( 'process','Offer Management'),

 ( 'expertise','Beginner'),
 ( 'expertise','Competent'),
 ( 'expertise','Expert'),

 ( 'experienceRange', '0 - 3 yrs'),
 ( 'experienceRange', '4 - 7 yrs'),
 ( 'experienceRange', '8 - 12 yrs'),
 ( 'experienceRange', '13 - 15 yrs'),
 ( 'experienceRange', '17 - 20 yrs'),
 ( 'experienceRange', '20+ yrs');


-- max limits for various parameters
insert into CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE) values
('maxScreeningQuestionsLimit',10),
('dailyCandidateUploadPerUserLimit',500),
('dailyCandidateInviteLimit',500),
('candidatesPerFileLimit',100),
('sendEmail',1),
('sendSms',1);


 -- key skill master data
insert into skills_master (skill_name) values
('Core Java'),
('Advance Java'),
('J2EE'),
('Spring'),
('Hibernate'),
('Struts'),
('Swing'),
('MultiThreading'),
('Java Beans'),
('JSF'),
('JSP'),
('JDBC'),
('JMS'),
('Servlets'),
('Collections'),
('WebServices - Soap'),
('WebServices - Rest'),
('EJB'),
('Play'),
('JavaFX'),
('Netty'),
('Wicket'),
('JMX'),
('JAF'),
('JPA'),
('Ant'),
('Maven'),
('Gradle'),
('Tomcat'),
('JBoss'),
('GlassFish'),
('WebLogic'),
('TestNG'),
('Cactus'),
('EasyMock'),
('Mockito'),
('Cucumber'),
('Javascript - AngularJS'),
('Javascript - NodeJS'),
('Javascript - ExtJS'),
('Javascript - BackboneJS'),
('Javascript - DOJO'),
('Javascript - Jquery'),
('Javascript - EmberJS'),
('Javascript - ReactJS'),
('Javascript - KnockoutJS'),
('HTML'),
('HTML 5'),
('CSS'),
('Photoshop'),
('UX Designer'),
('CorelDraw'),
('Silverlight'),
('AJAX'),
('Javascript - Sails'),
('Javascript - Express'),
('Javascript - Grunt/Gulp'),
('Javascript - Brocolli'),
('Javascript - Webpack'),
('Javascript - Jasmine'),
('Javascript - Karma'),
('Javascript - Mocha'),
('.Net '),
('.Net - C#.Net'),
('.Net - VB.Net'),
('WCF'),
('WPF'),
('TFS'),
('VB'),
('MVC'),
('ADO'),
('Windows'),
('Azure'),
('C#'),
('Sharepoint'),
('Excel'),
('TSQL'),
('SQL Server'),
('SQL Server SSRS'),
('SQL Server SSAS'),
('SQL Server SSIS'),
('TFS'),
('ASP'),
('Exchange'),
('ASP.NET Web API (2)'),
('ASP.NET MVC'),
('ASP.NET Web Forms (old school)'),
('nHibernate'),
('Entity Framework'),
('Linq2SQL'),
('ADO.NET'),
('nUnit'),
('SpecFlow'),
('MStest'),
('VC++'),
('STL'),
('Boost'),
('QT'),
('vxWidgets'),
('Loki'),
('POCO'),
('OpenCV'),
('VC'),
('Big Data - HBase'),
('Big Data - Hadoop'),
('Big Data - Hive'),
('Big Data - Spark'),
('Big Data - MapReduce'),
('Big Data - HDFS'),
('Big Data - MongoDB'),
('Oracle'),
('Oracle - BAM'),
('Oracle - Financials'),
('Oracle - Middleware'),
('Oracle - ORMB'),
('Oracle - SQL'),
('Oracle - Essbase'),
('MySQL'),
('NoSQL'),
('NoSQL - Cassandra'),
('NoSQL - MongoDB'),
('NoSQL - Berkeley DB'),
('NoSQL - Redis'),
('NoSQL - Riak'),
('NoSQL - CouchDB'),
('NoSQL - DynamoDB'),
('MySQL Stored Procedures'),
('MSSQL'),
('SQL'),
('DB2'),
('Postgresql'),
('Sybase'),
('Architecture'),
('Access'),
('SQLite'),
('Windows'),
('Linux'),
('RedHat RHEL'),
('Solaris'),
('Unix'),
('Android'),
('Mac/iOS'),
('Manual'),
('UAT'),
('Automation - Selenium'),
('Automation - Required'),
('Automation - Jasmine'),
('Automation - JUnit'),
('Automation - Protactor'),
('Automation - Silk Test'),
('Automation - Sahi Pro'),
('Automation - WebDriver'),
('Automation - SoapUI'),
('Automation - Watir'),
('Automation - Watin'),
('Automation - QTP'),
('Automation - Cucumber'),
('Automation - IBM Rational Functional Tester'),
('Automation - Appium'),
('Validation and Verification'),
('Performance - JMeter'),
('Performance - HP QC'),
('Performance - NeoLoad'),
('Performance - LoadRunner'),
('Performance - LoadUI'),
('Performance - Siege'),
('Performance - Gatling'),
('Infrastructure - SFTP'),
('Infrastructure - Vmware'),
('Infrastructure - SCCM'),
('Infrastructure - SCOM'),
('Infrastructure - Websence'),
('Infrastructure - Active Directory'),
('Infrastructure - Xendesktop'),
('Infrastructure - Netscalar'),
('Infrastructure - DNS'),
('Infrastructure - Web DMZ'),
('Infrastructure - ITIL'),
('Infrastructure - Application Packaging'),
('Application - JSB'),
('Application - Siebel'),
('Production'),
('Reporting - Regulatory Reporting'),
('Financial Planning and Analysis'),
('Trade Finance - Collateral Management'),
('Trade Finance'),
('Trade Finance - Settlement'),
('Risk - Market Risk'),
('Risk - Credit Risk'),
('CA'),
('Accounting'),
('PMO'),
('Fund Acounting'),
('Fund Acounting'),
('Internal Audit'),
('Finance Audit'),
('IT Audit'),
('NGO Audit'),
('HR'),
('ETL'),
('Informatica'),
('Pentaho'),
('Cognos'),
('Abinitio'),
('Business Objects'),
('Chef'),
('Puppet'),
('SVN'),
('Ansible'),
('Jenkins'),
('Artifactory'),
('Integration - Continuous'),
('Integration - Tools'),
('Nexus'),
('KVM'),
('Xen'),
('Xen'),
('VirtualBox'),
('Vagrant'),
('CFEngine'),
('SaltStack'),
('RANCID'),
('Ubuntu Juju'),
('RANCID'),
('New Relic'),
('Nagios'),
('Icinga'),
('Graphite'),
('Ganglia'),
('Cacti'),
('PagerDuty'),
('Sensu'),
('TCP/IP'),
('HTTP'),
('HTTP/2'),
('WebSockets'),
('Web Servers - Apache'),
('Web Servers - Tomcat'),
('Web Servers - IIS'),
('Web Servers - jBoss'),
('Scrum Master'),
('PMO'),
('PM'),
('Technical'),
('Business Analysis'),
('Perl'),
('Shell'),
('Python'),
('Ruby'),
('Bash'),
('AWK'),
('Python'),
('Ruby'),
('Lua'),
('Sinatra'),
('Padrino'),
('JRuby'),
('RSpec'),
('Capybara'),
('Watir'),
('Cucumber'),
('Passenger'),
('Capistrano'),
('Cake PHP'),
('Lucene'),
('Codeignitor'),
('Wordpress'),
('Joomla'),
('Symfony (2)'),
('Zend Framework (2)'),
('Yii'),
('Laravel'),
('Twig (templating)'),
('PHPUnit (testing)'),
('Doctrine (ORM)'),
('PHPStorm (IDE)'),
('Pear'),
('Smarty'),
('FICO'),
('ABAP'),
('HANA'),
('ME/MII'),
('Security'),
('MM'),
('SAP - Basis'),
('SAP - SCM'),
('GIT'),
('Subversion'),
('Endevor'),
('Mercurial'),
('SVN'),
('CVS'),
('Perforce'),
('Bitbucket'),
('Gitlab'),
('Goldensource'),
('Illustrator'),
('Flex'),
('CS'),
('Jasper/iReport'),
('Jira'),
('Pega'),
('TIBCO'),
('Tivoli'),
('Tableau'),
('TeamCity'),
('Murex'),
('Apache Kafka'),
('Memcached'),
('Nginx'),
('Varnish'),
('Squid'),
('haproxy'),
('CDN'),
('Big Data - Pig'),
('Big Data - Apache HBase'),
('Big Data - Cassandra'),
('Big Data - MapReduce'),
('Big Data - Spark'),
('TFS'),
('CruiseControl'),
('Bamboo'),
('Hudson'),
('Travis'),
('Mantis'),
('Confluence'),
('RedMine'),
('YouTrack'),
('Trello'),
('BaseCamp'),
('Visual Studio'),
('Eclipse'),
('NetBeans'),
('Aptana'),
('xCode'),
('WebStorm'),
('IntelliJ IDEA'),
('LAMP'),
('MVC'),
('OOPs'),
('SaaS'),
('Text NLP'),
('Ruby On Rails'),
('Swift'),
('XML'),
('JSON'),
('Scala'),
('Erlang'),
('OCaml'),
('Haskell'),
('F#'),
('Clojure'),
('Elixir'),
('Joomla'),
('Drupal'),
('Wordpress'),
('Magento (for e-commerce)'),
('Django'),
('Flask'),
('Pyramid'),
('Pylons'),
('SQLAlchemy'),
('web2py'),
('Kivy'),
('Akka'),
('Cats'),
('Lift'),
('Play'),
('Scalaz'),
('Slick'),
('Shapeless'),
('Spray'),
('iOS - Objective C'),
('iOS - Swift'),
('iOS - CoreData'),
('iOS - Cocoa'),
('iOS - Cocoa Touch'),
('iOS - UIKit'),
('iOS - CoreLocation'),
('iOS - CoreFoundation'),
('iOS - CoreImage'),
('iOS - CoreGraphics'),
('iOS - Cocoa Pods'),
('iOS - Flurry'),
('iOS - TestFlight'),
('iOS - HockeyApp'),
('Android - SDK'),
('Android - NDK'),
('Android - IDE'),
('Android - Gradle'),
('Android - AndEngine'),
('Android - Robotium'),
('jQuery Mobile'),
('PhoneGap'),
('Sencha Touch'),
('Dojo Mobile'),
('Titanium'),
('Xamarin'),
('OOD'),
('Design Patterns'),
('MVC'),
('SOA'),
('UML'),
('Webservices'),
('Markup'),
('Markup - XML'),
('Markup - XSLT'),
('Markup - xPath'),
('Markup - XMPP'),
('API'),
('API - Facebook API'),
('API - Google API'),
('API - Twitter API'),
('Agile'),
('Accounts Payable'),
('Asset Management'),
('Automation - Appium'),
('Business Intelligence'),
('Bootstrap'),
('C++'),
('C'),
('Chatbot'),
('CICS'),
('Citrix'),
('Claims'),
('Cloud Computing'),
('COBOL'),
('Data Analysis'),
('Data Mining'),
('Data Modelling'),
('Data Science'),
('Digital'),
('ERP/CRM'),
('Firewall'),
('Firewall'),
('Forecasting'),
('Fund Management'),
('GoldenGate'),
('Grails'),
('Hyperion'),
('Indirect Taxation'),
('Internet Protocol'),
('Marketing'),
('Digital'),
('Marketing-Head'),
('Maximo'),
('MDM'),
('Microservices'),
('MicrosoftExchange'),
('MongoDB'),
('NAT'),
('Neoload'),
('Neoload'),
('Security'),
('Networking'),
('OBIEE'),
('OFSAA'),
('OIPA'),
('OMNI'),
('Oozie'),
('OracleFusion'),
('ORMB'),
('Peoplesoft'),
('PHP'),
('QTP'),
('Recruitment'),
('Regulatory Reporting'),
('Requirement Gathering'),
('Risk'),
('Robotic Procees Automation'),
('Sailpoint'),
('Salesforce'),
('SAN'),
('SAP'),
('SAP - ICM'),
('SAP - R3'),
('SAS'),
('SCCM'),
('Big Data - Sqoop'),
('Big Data - Sqoop'),
('ServiceDesk'),
('ServiceNow'),
('Statutory Audit'),
('Tally'),
('Technical Writer'),
('UI Designer'),
('VPN'),
('Web Servers - WebLogic'),
('WindowsFAX'),
('FreeSwitch');

INSERT INTO SMS_TEMPLATES (TEMPLATE_NAME, TEMPLATE_CONTENT) VALUES
('ChatInvite','New Job - [[${commBean.jobTitle}]] at [[${commBean.senderCompany}]]%n[[${commBean.receiverFirstName}]],
%n[[${commBean.senderFirstName}]] from [[${commBean.senderCompany}]] has invited you to apply for the [[${commBean.jobTitle}]] position. Click the link below to apply.%n[[${commBean.chatLink}]]'),
('ChatCompleted','Congratulations [[${commBean.receiverFirstName}]]!%nYour Profile is now complete for the [[${commBean.jobTitle}]] position. [[${commBean.senderFirstName}]] from [[${commBean.senderCompany}]] will be in touch with you if your profile is shortlisted.%n%nGood luck!'),
('ChatIncompleteReminder1','Oh no [[${commBean.receiverFirstName}]]!  The Litmus Profile you started creating to for the [[${commBean.jobTitle}]] job at [[${commBean.senderCompany}]] was left incomplete. It''s important that you finish the profile to be considered for the job. Continue from where you left last. Just click the link to continue. [[${commBean.chatLink}]]'),
('ChatIncompleteReminder2','[[${commBean.receiverFirstName}]],%nThis is your final reminder%n[[${commBean.senderCompany}]] is still waiting to see your profile for the [[${commBean.jobTitle}]] job. Your Litmus Profile is not complete. It will take just a few more minutes to finish it. Please click the link to continue. [[${commBean.chatLink}]]'),
('LinkNotVisitedReminder1','[[${commBean.jobTitle}]] Job opportunity at [[${commBean.senderCompany}]]. [[${commBean.receiverFirstName}]], you are being considered for this job. Click the link to apply. [[${commBean.chatLink}]]'),
('LinkNotVisitedReminder2','[[${commBean.receiverFirstName}]], are you not interested in this job? [[${commBean.senderFirstName}]] from [[${commBean.senderCompany}]] has invited you to apply for the [[${commBean.jobTitle}]] position. Click the link below to apply. [[${commBean.chatLink}]]'),
('ChatNotVisitedReminder1','Hi [[${commBean.receiverFirstName}]],%nHere is your link to create your Litmus Profile for [[${commBean.jobTitle}]] job at [[${commBean.senderCompany}]]. It''s required to submit completed profile to be considered for the job. The link is valid only for 48 hours. Click the link to begin. [[${commBean.chatLink}]] '),
('ChatNotVisitedReminder2','[[${commBean.receiverFirstName}]],%nJust a reminder to complete your Litmus Profile for [[${commBean.jobTitle}]] job at [[${commBean.senderCompany}]]. It will take just a few minutes to finish it.  It''s required that you finish the profile to be considered for the job.  This link will expire in 24 hours.%nClick the link to apply. [[${commBean.chatLink}]] ');
