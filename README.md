# ASPECT: Student Academic Management and Grade Processing System

## Project Overview
ASPECT is a desktop-based student academic management system built with Java Swing and MySQL. It utilizes a strict 3-tier architecture (Presentation, Business Logic, Data) and implements Role-Based Access Control (RBAC) to handle the complete grade lifecycle from calculation to official university posting.

## Team Members (Group 8)
* ANG, KARL SPENCER Y. 
* AQUINO, FATH DAYNIA D.
* CRUZ, HENRICK PAUL C.
* VILLONES, LIAN MARCO N.
  
## Install:

- Java Development Kit compatible with Java 21
- Eclipse IDE or another compatible Java IDE
- MySQL Server 8.x
- MySQL Workbench (recommended)
- MySQL Connector/J

## In Eclipse:

1. Open **Run → Run Configurations**
2. Select the ASPECT Java application
3. Open the **Environment** tab
4. Add the three variables above
5. Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password
6. Apply the changes and run ASPECT

> The source project's built-in fallback URL may still reference a development database. Setting `GRADING_DB_URL` explicitly to `ASPECT_DB` avoids depending on that fallback.

## Configure MySQL Connector/J

The Eclipse project may contain a Connector/J classpath entry that points to the original developer's Windows installation.

If Eclipse reports that the MySQL library is missing:

1. Download/install MySQL Connector/J
2. Right-click the project in Eclipse
3. Choose **Build Path → Configure Build Path**
4. Remove the unavailable Connector/J entry if necessary
5. Choose **Add External JARs**
6. Select your local `mysql-connector-j-*.jar`
7. Apply and close

## Run ASPECT

Start MySQL Server first, then run the application's main/login class from Eclipse.

## Key Features
* **Three-Role Hierarchy:** Distinct dashboards for Registrar, Teacher, and Student.
* **Automated Grade Computation:** Calculates CO percentages and converts to the Mapúa 1.00 - 5.00 grading scale.
* **Curriculum Tracking:** Color-coded academic progress tracker for enrolled and credited subjects.
* **Hardware Printing Integration:** Generates a physical Certificate of Matriculation (CM).
* **Grade Concern Workflow:** Built-in ticketing system for students to dispute draft grades.

## Database Setup
1. Create a new schema named `GradingSystem` in MySQL.
2. Import the `database/Grp8_ASPECT_Database` file to build the tables and populate the sample data.
3. Update the `GRADING_DB_PASSWORD` in `DBConnection.java` to match your local MySQL configuration.

A typical end-to-end workflow is:

Registrar creates Student Account
        ↓
Student changes temporary password
        ↓
Student requests a subject
        ↓
Teacher approves enrollment
        ↓
Teacher manages grades and OLA
        ↓
Teacher submits the whole class
        ↓
Registrar reviews the submission
        ↓
   Return or Post
        ↓
If Returned → Teacher corrects and resubmits
        ↓
Registrar Posts
        ↓
Student sees official grades and performance
```
