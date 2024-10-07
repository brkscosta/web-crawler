# Web Crawler

## Overview
This project is a web crawler application written in Java using Maven. It is designed to traverse web pages, collect data, 
and build a graph of web pages and links.

## Features
- **Placement Strategies**: Supports different placement strategies for web pages, including circular and random.
- **Search Criteria**: Implements various search criteria such as depth-first, breadth-first, and interactive search.
- **Graph Management**: Provides functionalities to clear and retrieve the graph of web pages and links.
- **Logging**: Logs important events and actions during the crawling process.

## Project Structure

The project is structured as follows:

- **app**: Contains the code related to the application such as Views and ViewModels.
- **data**: Contains entities and repositories to manage the data of the application.
- **domain**: Contains the core logic of the search, BFS, DFS, and interactive search.

The uses the MVVM (Model-View-ViewModel) architecture pattern to separate the concerns of the application. Also uses the Singleton design
pattern to ensure that only one instance of the application is created.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Project
To build the project, run the following command:
```sh
mvn clean install