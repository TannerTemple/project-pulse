# Project Pulse — Vision and Scope

**Version 1.0**

---

## 1. Introduction

This document defines the goals, purpose, and boundaries of Project Pulse. It identifies the business problems the software solves, describes the future vision for how it fits into the domain, and specifies the project scope.

### 1.1 Background
The Department of Computer Science at TCU offers a senior design course where students in teams solve real-world software problems. However, tracking individual contribution and team dynamics is challenging. Two manual tools are currently in use:

**Weekly Activity Report (WAR)** — Each student manually edits a shared Google Sheets document weekly.

**Peer Evaluation Form** — Each student downloads an Excel spreadsheet, fills it out, and uploads it to TCU Online. The instructor then downloads all submissions, runs a Java program to calculate results, and re-uploads grades.

### 1.2 Current Process Flows (As-Is)

#### WAR Process
Each Monday after the project begins, every student accesses the shared Google Sheet for their team, finds the correct week sheet, and documents their activities. The instructor reviews updated sheets on Tuesday, grades them, and uploads feedback to TCU Online.

**Pain points:**
- Manual, error-prone editing of shared Google Sheets
- Students may fill out the wrong sheet or make formatting errors
- No centralized dashboard for the instructor

#### Peer Evaluation Process
Each Tuesday, students review their team's WAR, fill out an Excel peer evaluation form, and upload it to TCU Online. The instructor downloads all forms, runs a parsing program, calculates grades, and re-uploads results.

**Pain points:**
- Spreadsheets with incorrect formatting or missing columns cause parsing errors
- Highly time-consuming for the instructor (download → parse → re-upload every week)
- No way for students to view their evaluations in real time

---

## 2. Business Requirements

### 2.1 Problem Statement
The current peer evaluation process at TCU CS is burdened by inefficiencies, errors, and delays. Students struggle with downloading, completing, and uploading forms. Faculty manually manage evaluations, causing delayed feedback. This hinders academic growth and misallocates resources.

### 2.2 Business Objectives

| ID | Objective |
|---|---|
| BO-1 | Reduce the instructor's time to grade peer evaluations by **50%** |
| BO-2 | Increase WAR and peer evaluation submission rates by **20%** |
| BO-3 | Reduce the time for students to complete WARs and peer evaluations by **25%** |

### 2.3 Vision Statement

> **For** students of TCU Senior Design  
> **Who** need an easier way to submit and update weekly activity reports and peer evaluations,  
> **Project Pulse** makes it easier for students to submit WARs and peer evaluations and for instructors to view and grade them,  
> **Unlike** the traditional manual process,  
> **Our product** streamlines the whole workflow, making it more accessible and painless for both students and instructors.

### 2.4 Proposed Process Flows (To-Be)

#### Improved WAR Process
Students and instructors complete all WAR tasks entirely within Project Pulse, eliminating the need for Google Sheets. The only remaining manual task is the instructor uploading final grades to TCU Online's LMS (outside the scope of this project).

#### Improved Peer Evaluation Process
1. Students review the team WAR and submit peer evaluations within Project Pulse.
2. Project Pulse automatically compiles evaluations, calculates scores, and generates feedback.
3. The instructor reviews aggregated scores and feedback directly in the system.
4. Students independently view their scores and feedback on demand.
5. The instructor manually uploads final grades to TCU Online (outside scope).

### 2.5 Business Risks

| ID | Risk | Probability | Impact |
|---|---|---|---|
| RI-1 | Annual cloud hosting fees must be covered by the CS Department | Medium | Medium |
| RI-2 | Student peer evaluation data could be breached | Low | High |
| RI-3 | The application could be more confusing than the current method | Low | Medium |
| RI-4 | System may be too TCU-specific to generalize to other courses | Medium | Low |

### 2.6 Assumptions and Dependencies

| ID | Description |
|---|---|
| AS-1 | The system uses technologies the client has knowledge of and can maintain post-delivery |

---

## 3. Stakeholders

| Stakeholder | Value | Features of Interest | End User? |
|---|---|---|---|
| **Students** | Easier submission, single platform, no file downloads | Submit WARs & peer evals in one place | Yes |
| **Instructors** | Automated grading, real-time dashboard, team insights | View WARs, generate peer eval reports | Yes |

### 3.1 User Environment
Users access the application via a web browser on desktop, laptop, or mobile — any OS.

### 3.2 Alternatives
The status quo: students upload Excel files to TCU Online; instructors manually parse and re-upload grades. The alternative is slower, error-prone, and does not scale.

---

## 4. Scope and Limitations

### 4.1 Product Perspective
Project Pulse is a standalone web application. It integrates with **Gmail** to send automated invitation and notification emails. The instructor manually uploads final grades to TCU Online — that integration is out of scope.

### 4.2 Major Features

| ID | Feature |
|---|---|
| FE-1 | Manage senior design sections, teams, and students |
| FE-2 | Submit weekly activity reports and peer evaluations |
| FE-3 | Generate WAR and peer evaluation grade reports for the section |

### 4.3 Deployment
The system is hosted on **Microsoft Azure**. See the Use Cases document for detailed functional requirements.
