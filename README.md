# Project 5 
- ### NOTE: To access the links on AWS, you must connect to the UCI VPN.
- #### Team: pressbuttonwinhaha
    
    - #### Names: Kevin Wu, Joseph Ziller
    
    - #### Project 5 Video Demo Link: https://youtu.be/boGaqQNcF0g

    - #### Instruction of deployment:
      - Create a new database called 'moviedb' and its tables with the createtable.sql file.
      - Then populate your database with the movie-data.sql file.
      - After populating your database, be sure to encrypt your passwords (both the customers and employees table)
      - If on a local machine, you can run the program on IntelliJ with Maven and Tomcat configured. 
      - If on AWS, use the "mvn package" command in the project directory where pom.xml is located to compile the Java code. Then move or copy the .war file to the var/lib/tomcat10/webapps directory. 
      - Only CSS, JS, Java, and HTML were used. (Python is used for the log_processing script but that should not hinder you running the project)

    - #### Collaborations and Work Distribution:
      - Kevin Wu: 
        - JMeter testing
        - Connection Pooling
        - Setting up AWS Instances for Load Balancing and Master-Slave Replication
        - Updating README answering questions
      - Joseph Ziller:
        - JMeter testing
        - Connection Pooling
        - Setting up AWS Instances for Load Balancing and Master-Slave Replication
        - Updating README answering questions
      - We mostly worked together for the most part to get everything done.

- # Connection Pooling
  - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - [The context.xml file is the most crucial part of implementing Connection Pooling](WebContent/META-INF/context.xml) 
    - [AddMovieServlet](src/AddMovieServlet.java)
    - [AddStarServlet](src/AddStar.java)
    - [AutoCompleteServlet](src/AutoCompleteServlet.java)
    - [CheckoutServlet](src/CheckoutServlet.java)
    - [ConfirmationPageServlet](src/ConfirmationPageServlet.java)
    - [DashboardServlet](src/DashboardServlet.java)
    - [IndexServlet](src/IndexServlet.java)
    - [LoginServlet](src/LoginServlet.java)
    - [MoviePageServlet](src/MoviePageServlet.java)
    - [ShoppingCartServlet](src/ShoppingCartServlet.java)
    - [SingleMoviePageServlet](src/SingleMoviePageServlet.java)
    - [SingleStarServlet](src/SingleStarServlet.java)
    - [SortServlet](src/SortServlet.java)
    - [Top20Servlet](src/Top20Servlet.java)
  - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - Connection Pooling is utilized in the Fabflix code by first calling the init() method on the Servlets. In the init() method, the Servlet creates a new DataSource which is pre-defined in [](WebContent/META-INF/context.xml). This allows the Servlet to call .getConnection() on the DataSource, thereby reusing an existing connection or opening a new one if none are available to make SQL calls to.
    - When a Servlet establishes a connection, it either creates a new one or looks for an existing, available connection from the pool. If getting one from the pool, the connection does not need to be established again. Thus, the time taken to establish new connections is drastically reduced.

  - #### Explain how Connection Pooling works with two backend SQL.
    - Our two backend SQL databases are in the form of a Master-Slave Replication. Since both of deployed war files in the Master and Slave use Connection Pooling, both backend SQL databases will have a pool of connections in which our Servlets can use frequently, speeding up SQL processes.

- # Master/Slave
  - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - [The context.xml file is also crucial in routing queries to Master/Slave SQL Databases](WebContent/META-INF/context.xml)
    - [AddMovieServlet](src/AddMovieServlet.java)
    - [AddStarServlet](src/AddStar.java)
    - [CheckoutServlet](src/CheckoutServlet.java)

  - #### How read/write requests were routed to Master/Slave SQL?
    - Our Load Balancer will route requests to either the Master/Slave SQL. This is to ensure good consistency and to make sure neither Database is too overloaded while also working on requests.
    - If the request is a read request, it can go to either the Master or Slave. 
    - If the request is a write request, our [context.xml](WebContent/META-INF/context.xml) ensures that all Servlets that do any writing to the Database is sent to the Master Database only.
    
- # JMeter TS/TJ Time Logs
  - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    - First move the [log_processing.py](log_processing.py) file to any desired directory. 
    - Then, open command line prompt and cd to that directory and type in "python (or python3) log_processing.py [Path/To/Log/Here]" to run the script.
    - You can type in multiple files like so: python log_processing.py filePath1 filePath2 ... filePathN to combine all the files' TS and TJ together.
    - NOTE: [log_processing.py](log_processing.py) does not do any error handling (aside from checking if the file exists). If your log files do not match the format below, then an Exception will most likely occur.
      - #### Format of each line should be: TS ts_time_here TJ tj_time_here
    
- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot**                                                                                                                                                                                               | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                                                                                                                                                                                 |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|-------------------------------------|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | ![Single HTTP/1 thread](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Single%20Instance%20HTTP%201%20Thread.png)                                                       | 88 ms                      | 17.271 ms                           | 17.118 ms                 | The fastest and most basic of test cases. One thread means one user using the single instance as intended. This test case benefits greatly from Connection Pooling, however, since every time the thread makes a request to the Search Servlet, an existing connection to MySQL can be reused. We can think of this case as our control group to compare with the other test cases in the rest of the table. |
| Case 2: HTTP/10 threads                        | ![Single HTTP/10 threads](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Single%20Instance%20HTTP%2010%20Threads.png)                                                   | 197 ms                     | 126.295 ms                          | 126.192 ms                | We can see the effects of adding 10 threads (users) to a single instance. This drastically slow speeds down and we get an average query time over twice that of the 1 thread test case. However, the average query time is still not as slow as expected, but more threads will continue to increase the average query time.                                                                                 |
| Case 3: HTTPS/10 threads                       | ![Single HTTPS/10 threads](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Single%20Instance%20HTTPS%2010%20Threads.png)                                                 | 199 ms                     | 129.573 ms                          | 129.402 ms                | As expected, 10 threads with HTTPS was slower than 10 threads with HTTP, but not by a large margin. Since HTTPS has increased computational complexity and security, this decrease in speed makes sense.                                                                                                                                                                                                     |
| Case 4: HTTP/10 threads/No connection pooling  | ![Single HTTP/10 threads/No connection pooling](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Single%20Instance%20HTTP%2010%20Threads%20NO%20CONNECTION%20POOLING.png) | 850 ms                     | 776.150 ms                          | 765.672 ms                | The slowest average query time out of all cases. Having no connection pooling implemented means that it is very costly everytime the search servlet is called since a new connection to MySQL has to be created, causing massive amounts of overhead. Furthermore, since there are 10 threads, the single instance may be overloaded with the amount of incoming requests and therefore run much slower.     |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot**                                                                                                                                                                                               | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|-------------------------------------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | ![Scaled HTTP/1 thread](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Scaled%20Instance%20HTTP%201%20Thread.png)                                                       | 92 ms                      | 17.770 ms                           | 17.583 ms                 | Very similar to the Single Instance HTTP 1 Thread Case, just slightly slower. Using 1 thread on the scaled instance is pointless as we do not gain any benefit that load balancing provides. The reason for the slightly slower average query time is potentially because all requests to the Search Servlet must first go through the Load Balancer, which adds a little extra time.                                                                |
| Case 2: HTTP/10 threads                        | ![Scaled HTTP/10 threads](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Scaled%20Instance%20HTTP%2010%20Threads.png)                                                   | 140 ms                     | 67.966 ms                           | 67.831 ms                 | Because of the load balancing and connection pooling, 10 threads on the scaled instance has a smaller average query time 10 threads on the single instance. Servlets are able to reuse connections due to connection pooling and the load balancing helps control traffic evenly, reducing the average search query time.                                                                                                                            |
| Case 3: HTTP/10 threads/No connection pooling  | ![Scaled HTTP/10 threads/No connection pooling](https://github.com/uci-jherold2-fall23-cs122b/2023-fall-cs122b-pressbuttonwinhaha/blob/master/img/Single%20Instance%20HTTP%2010%20Threads%20NO%20CONNECTION%20POOLING.png) | 296 ms                     | 221.284 ms                          | 219.644 ms                | Compared to the single instance case with 10 threads, this is a massive improvement. While this case does have the slowest average query time out of all scaled instance tests, we can see the effects of load balancing working immensely here. Instead of 10 threads sending requests all to a single instance, the load balancer can split requests to two separate instances so that each instance does half the work, improving speeds greatly. |
-------
# [Project 4 Demo Video](https://www.youtube.com/watch?v=gZP35_y2Qzc)
Note on connecting to AWS with Android: If you look in "Android/app/src/main/java/edu/uci/ics/
fabflixmobile/data/constants/BaseURL.java", you can see that HTTPS is used and the AWS Public IP is
used instead of localhost's IP.
## Group Members

Kevin Wu: Worked on fulltext and autocomplete for both Android and webpage, worked on frontend

Joseph Ziller: Completed the Android app's pages, also worked on frontend for both webpage and Android

-------
# [Project 3 Demo Video](https://www.youtube.com/watch?v=KEf2JUiEb9Q)

You can find stored-procedure.sql in the files.

## Group Members

Kevin Wu: Worked on XML Parser and Insertions, setting up LoginServlet with reCAPTCHA and encrypted passwords, setup HTTPS

Joseph Ziller: Worked on the Employee Dashboard, adding movies and adding stars from dashboard,
worked on frontend and backend servlets for adding stars and movies

## Prepared Statements Locations
PreparedStatements are located in:

AddMovieServlet, AddStar, CheckoutServlet, ConfirmationPageServlet, DashboardServlet, IndexServlet, LoginServlet,
MoviePageServlet, ShoppingCartServlet, SingleMoviePageServlet, SingleStarServlet, Top20Servlet

## Parsing Optimization Report

With no optimizations (just straight parsing and insertions), runtime took around 10 minutes on AWS.

With one optimization of doing SQL Batch Insertions, runtime was improved by around 2-3 minutes and took 7-8 minutes on AWS.

With two optimizations of doing SQL Batch Insertions and Multithreading the SQL insertions, runtime was massively improved by around 3 minutes
and took around 4.5 minutes on AWS.

## Parsing Inconsistency Report

* Inserted 6838 stars
* Inserted 51 genres
* Inserted 12030 movies
* Inserted 9834 genres in movies
* Inserted 29539 stars in movies
* 19 movies inconsistent
* 29 movies duplicate
* 3313 movies have no genres
* 4642 movies have no stars
* 66 directors not found
* 737 movies not found
* 18256 stars not found
* 25 stars duplicate

-------
# [Project 2 Demo Video](https://www.youtube.com/watch?v=Xfx1SACVeOA)

## Group Members

Kevin Wu: Worked on Login, Movie List Page, Searching, epxanded Single Pages and frontend

Joseph Ziller: Worked on Shopping Cart, Checkout, Confirmation Page, and frontend

Substring matching for Searching works by finding all strings that contain the pattern like so: %pattern%.
For example, if you searched the title "term", this will show up in SQL as LIKE %Term% and will look for all movies with the substring "term"
anywhere in the movie title.

--------
# [Project 1 Demo Video](https://www.youtube.com/watch?v=3ZAeWEAck6M)
## Group Members

Kevin Wu: Created SQL relations, worked on Movie List Page and Single Star Page, worked on designing frontend

Joseph Ziller: Worked on Single Movie Page and worked on designing frontend

--------
No special instructions to deploy and run.