# Ciguan

Ciguan is a Java framework for rapid client application development where both time to market and rich functionality are key success factors. Ciguan is derived from the core module of a web framework developed by Cinnober Financial Technology which is used in large-scale enterprise financial systems. User interface (UI) elements and their behavior are defined in configuration files. Application configuration can be divided into different modules.

The data in the application is stored in data sources which are containers for a specific type of object. Data sources can either be defined in configuration files or implemented in Java. A data source can have filters for properties and access rights. A client can subscribe to changes in data sources and will be notified when such changes occur. 

Virtual properties, denoted as 'get methods', can be defined for the objects in the data sources. When referencing the property, the get method is invoked, and can then look up and return its data from virtually any location.

Metadata is used to describe different aspects of objects in the data sources. This data can be used when formatting or parsing the object properties. By defining metadata, you make the client aware of the structure of a server side object, thus enabling it to for example automatically assign relevant input fields to attributes. Every class that you intend to use as a model object in a view must have metadata created. Ciguan automatically creates metadata for all view models, all context object classes, and all data source item classes. 

Ciguan provides a plug-in architecture for communication with back-end systems, and is not tied to any specific protocol.



## Learning about Ciguan

To learn more about Ciguan please see our documentation: [Ciguan developers guide](doc/ciguan-dev-guide.pdf)

## Licence

Ciguan is licensed under the MIT license.

![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)


## Team Staff

* Magnus Lenti
* Anders Thyberg
* Pär Westblad
* Stefan Bylund


## Alumni Group

* Jörgen Ekroth
* Patrik Axelsson
* Marcus Heikkinen
* Crina Toth
