# Project Pulse — Use Cases

**Version 1.0**

---

## Use Case List

| Actor | Use Cases |
|---|---|
| **Admin** | UC-1: Create a rubric · UC-2: Find sections · UC-3: View a section · UC-4: Create a section · UC-5: Edit a section · UC-6: Set up active weeks · UC-7: Find teams · UC-8: View a team · UC-9: Create a team · UC-10: Edit a team · UC-11: Invite students · UC-12: Assign students to teams · UC-13: Remove a student from a team · UC-14: Delete a team · UC-15: Find students · UC-16: View a student · UC-17: Delete a student · UC-18: Invite instructors · UC-19: Assign instructors to teams · UC-20: Remove an instructor from a team · UC-21: Find instructors · UC-22: View an instructor · UC-23: Deactivate an instructor · UC-24: Reactivate an instructor |
| **Student** | UC-25: Set up a student account · UC-26: Edit account · UC-27: Manage WAR activities · UC-28: Submit peer evaluation · UC-29: View own peer evaluation report |
| **Instructor** | UC-30: Set up an instructor account · UC-31: Generate section peer evaluation report · UC-32: Generate team WAR report · UC-33: Generate student peer evaluation report · UC-34: Generate student WAR report |

---

## UC-1: Create a Rubric

**Actor:** Admin | **Priority:** High | **Frequency:** 1×/year

**Trigger:** Admin indicates to create a new rubric.

**Preconditions:** Admin is logged in.

**Postconditions:** New rubric is stored in the system.

**Normal Flow:**
1. Admin indicates to create a new rubric.
2. System prompts for rubric details.
3. Admin enters details and confirms.
4. System validates inputs.
5. System displays rubric summary and asks for confirmation.
6. Admin confirms (or returns to step 3 to modify).
7. System saves rubric and confirms creation.

**Extensions:**
- `4a`: Validation error → system alerts Admin, Admin corrects and retries.

**Details:**
- **Rubric name** — must be unique (e.g., `Peer Eval Rubric v1`)
- **Criteria** — each criterion has: name, description, max score (positive decimal)
- Default criteria: Quality of Work, Productivity, Initiative, Courtesy, Open-mindedness, Engagement in Meetings — each with max score 10

---

## UC-2: Find Senior Design Sections

**Actor:** Admin | **Priority:** High | **Frequency:** ~5×/week

**Trigger:** Admin indicates to find sections.

**Preconditions:** Admin is logged in.

**Postconditions:** Matching sections displayed (list may be empty).

**Normal Flow:**
1. Admin indicates to search.
2. System prompts for search criteria.
3. Admin enters criteria and confirms.
4. System finds and displays matches.

**Extensions:**
- `4a`: No results → system alerts Admin; Admin may create a section or retry.

**Search fields:** Section name (optional string)

**Display:** Section name, team names — sorted by section name descending, team names ascending.

---

## UC-3: View a Senior Design Section

**Actor:** Admin | **Priority:** High | **Frequency:** ~5×/week

**Trigger:** Admin chooses a section from search results.

**Normal Flow:**
1. Admin finds sections via UC-2.
2. Admin selects a specific section.
3. System displays: section name, start/end date, teams (with members and instructors), unassigned instructors, unassigned students, rubric used.

---

## UC-4: Create a Senior Design Section

**Actor:** Admin | **Priority:** High | **Frequency:** 1×/year

**Trigger:** Admin indicates to create a new section.

**Preconditions:** Admin is logged in.

**Postconditions:** New section stored in system.

**Normal Flow:**
1. Admin indicates to create a section.
2. System prompts for section details.
3. Admin enters name (e.g., `2023-2024`) and start/end dates.
4. System prompts Admin to select a rubric.
5. Admin selects an existing rubric; system displays its criteria.
6. Admin confirms rubric (or edits criteria — see `7a`).
7. System validates inputs and checks for duplicate section name.
8. System displays summary and asks for confirmation.
9. Admin confirms (or returns to modify).
10. System saves section.

**Extensions:**
- `5a`: No rubric exists → Admin creates one via UC-1.
- `7a`: Admin edits rubric criteria → system duplicates the rubric first (new rubric is created), Admin modifies, returns to step 6.
- `8a`: Validation error → Admin corrects and retries.
- `9a`: Duplicate section name → Admin corrects or cancels.

**Duplication rule:** Section name must be unique.

---

## UC-5: Edit a Senior Design Section

**Actor:** Admin | **Priority:** High | **Frequency:** ~1×/year

**Editable fields:** Section name, start/end dates, rubric.

**Normal Flow:**
1. Admin views section via UC-3.
2. Admin chooses to edit.
3. System displays editable fields.
4. Admin makes changes and confirms.
5. System validates, shows warnings, and asks for confirmation.
6. Admin acknowledges and confirms.
7. System saves changes.

---

## UC-6: Set Up Active Weeks

**Actor:** Admin | **Priority:** High | **Frequency:** 1×/year

**Preconditions:** At least one section exists.

**Trigger:** Admin indicates to configure active weeks for a section.

**Normal Flow:**
1. Admin selects a section.
2. System displays all weeks between start and end date.
3. Admin marks weeks as inactive (e.g., holiday break).
4. System displays active week summary; Admin confirms.
5. System saves active week configuration.

**Business Rule:** BR-2 — Students may only submit peer evaluations during active weeks, but can submit WAR activities at any time.

---

## UC-7: Find Senior Design Teams

**Actors:** Admin, Instructor | **Priority:** High | **Frequency:** ~2×/week

**Search fields:** Section ID, section name (optional), team name (optional), instructor (optional)

**Display:** Team name, description, website URL, members, instructors — sorted by section name descending then team name ascending.

**Extensions:**
- `4a`: No results → User may create a team (UC-9) or retry.

---

## UC-8: View a Senior Design Team

**Actors:** Admin, Instructor | **Priority:** High | **Frequency:** ~2×/week

**Normal Flow:**
1. User finds teams via UC-7.
2. User selects a specific team.
3. System displays: team name, description, website URL, members, instructors.

---

## UC-9: Create a Senior Design Team

**Actor:** Admin | **Priority:** High | **Frequency:** 5–10×/year

**Postconditions:** New team stored in system.

**Details:**
- Team name (must be unique)
- Team description
- Team website URL

**Duplication rule:** Team name must be unique across all sections.

---

## UC-10: Edit a Senior Design Team

**Actor:** Admin | **Priority:** High | **Frequency:** ~6×/year

**Editable fields:** Team name, description, website URL.

**Extension:**
- `6a`: Duplicate team name → Admin corrects and retries.

---

## UC-11: Invite Students to Join a Section

**Actor:** Admin | **Secondary:** Student | **Priority:** High | **Frequency:** 1×/year

**Trigger:** Admin indicates to invite students.

**Postconditions:** Invitation emails sent.

**Normal Flow:**
1. Admin provides student email addresses (semicolon-separated).
2. System validates format.
3. System displays email count and default message.
4. Admin confirms (or customizes message — see `6a`).
5. System sends one invitation email per address.

**Email format:** Semicolons as delimiters, spaces ignored.
- Valid: `a@tcu.edu; b@tcu.edu`
- Invalid: trailing semicolons, space-separated only

**Default email:**
```
Subject: Welcome to The Peer Evaluation Tool - Complete Your Registration

Hello,

[Admin Name] has invited you to join The Peer Evaluation Tool.
To complete your registration, please use the link below:

[Registration link]

Best regards,
Peer Evaluation Tool Team
```

Each invitation link is unique per student. After receiving the link, students complete UC-25.

---

## UC-12: Assign Students to Teams

**Actor:** Admin | **Priority:** High | **Frequency:** 1×/year

**Preconditions:** Teams created; students have registered accounts.

**Normal Flow:**
1. System displays lists of teams and students.
2. Admin assigns groups of students to teams.
3. System shows assignment summary; Admin confirms.
4. System notifies students of their assignment.

---

## UC-13: Remove a Student from a Team

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Postconditions:** Student removed from team.

**Normal Flow:**
1. Admin views team via UC-8.
2. Admin removes a student.
3. System shows updated roster; Admin confirms.
4. System notifies the student.

---

## UC-14: Delete a Senior Design Team

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Postconditions:** Team permanently deleted.

**Data rules:**
- Students and instructors are removed from the team first.
- All associated WARs and peer evaluations are also permanently deleted.

**Notifications:** All team members and instructors are notified.

---

## UC-15: Find Students

**Actors:** Admin, Instructor | **Priority:** High | **Frequency:** ~2×/week

**Search fields:** First name, last name, email, section name, team name (all optional).

**Display:** First name, last name, team name — sorted by section name descending then last name ascending.

---

## UC-16: View a Student

**Actors:** Admin, Instructor | **Priority:** High | **Frequency:** ~10×/week

**Displays:** First name, last name, section, team, peer evaluations, WARs.

---

## UC-17: Delete a Student

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Data rules:** All associated WARs and peer evaluations are permanently deleted with the student.

---

## UC-18: Invite Instructors to Register

**Actor:** Admin | **Secondary:** Instructor | **Priority:** High | **Frequency:** 1×/year

Same flow as UC-11 but for instructors. After receiving the link, instructors complete UC-30.

---

## UC-19: Assign Instructors to Teams

**Actor:** Admin | **Priority:** High | **Frequency:** 1×/year

**Business Rule:** BR-1 — Every team must have at least one instructor. An instructor can supervise multiple teams.

**Normal Flow:**
1. System displays lists of teams and instructors.
2. Admin assigns one or more instructors to each team.
3. System confirms; Admin approves.
4. System notifies assigned instructors.

---

## UC-20: Remove an Instructor from a Team

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Business Rule:** BR-1

**Normal Flow:** Admin views team → removes instructor → confirms → system notifies instructor.

---

## UC-21: Find Instructors

**Actor:** Admin | **Priority:** High | **Frequency:** ~3×/year

**Search fields:** First name, last name, team name, status (Active / Deactivated).

**Display:** First name, last name, team name, status — sorted by academic year descending then last name ascending.

---

## UC-22: View an Instructor

**Actor:** Admin | **Priority:** Medium | **Frequency:** ~5×/year

**Displays:** First name, last name, supervised teams (grouped by section), status (Active / Deactivated).

---

## UC-23: Deactivate an Instructor

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Normal Flow:**
1. Admin views instructor via UC-22.
2. Admin chooses to deactivate and enters a reason.
3. System warns of consequences; Admin confirms.
4. Instructor's account is disabled (data retained; recoverable).

---

## UC-24: Reactivate an Instructor

**Actor:** Admin | **Priority:** Low | **Frequency:** Rare

**Normal Flow:**
1. Admin views instructor via UC-22.
2. Admin chooses to reactivate; confirms.
3. System reactivates account and notifies instructor.

---

## UC-25: Set Up a Student Account

**Actor:** Student | **Priority:** High | **Frequency:** ~35–40×/year

**Trigger:** Student clicks the registration link in the invitation email.

**Normal Flow:**
1. Student clicks link.
2. System shows registration form.
3. Student enters: first name, last name, email (pre-filled), password.
4. System validates and displays summary; Student confirms.
5. System saves account and redirects to login.

**Extensions:**
- `2a`: Account already exists → system redirects to login.

---

## UC-26: Edit Account (Student)

**Actor:** Student | **Priority:** High | **Frequency:** Rare

**Editable fields:** First name, last name, email, password.

---

## UC-27: Manage WAR Activities

**Actor:** Student | **Priority:** High | **Frequency:** ~3×/week per student

**Trigger:** Student indicates to manage WAR activities.

**Preconditions:** Student is logged in.

**Postconditions:** Activity added, edited, or deleted.

**Normal Flow:**
1. System prompts student to select an active week (cannot select a future week).
2. System displays existing activities for that week.
3. Student chooses: Add / Edit / Delete.

**Add activity fields:**

| Field | Values |
|---|---|
| Category | DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, DOCUMENTATION, DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS |
| Activity | Short title |
| Description | Detailed description |
| Planned hours | Numeric |
| Actual hours | Numeric (nullable) |
| Status | IN_PROGRESS, UNDER_TESTING, DONE |

---

## UC-28: Submit Peer Evaluation

**Actor:** Student | **Priority:** High | **Frequency:** 1×/week per student

**Business Rules:** BR-3, BR-4

**Preconditions:** Student is logged in during an active week.

**Normal Flow:**
1. Student indicates to submit peer evaluation for the previous week.
2. System presents evaluation form for every team member (self included).
3. Student scores each teammate on all rubric criteria (integer scores) and optionally adds public/private comments.
4. Student confirms submission.
5. System saves the peer evaluation.

**Notes:**
- Every team member must be evaluated.
- Scores must be integers.
- Public comments are sent to the evaluatee; private comments are instructor-only.
- BR-3: Peer evaluations cannot be edited once submitted.
- BR-4: Evaluations are for the **previous** week only. Students have one week to submit; late submissions are not accepted.

---

## UC-29: View Own Peer Evaluation Report

**Actor:** Student | **Priority:** High | **Frequency:** ~1×/week

**Business Rule:** BR-5 — Students can only see their own scores, public comments, and overall grade (never evaluators' identities or private comments).

**Report parameters:** Active week (default: previous week).

**Report columns:** Student name, average criterion scores, public comments, overall grade.

**Grade algorithm:** For each criterion, average the scores from all teammates. Overall grade = average of all evaluators' total scores.

*Example:* Tim gives John 10+9+10+9+10+10=58; Lily gives 5+5+10+10+10+10=50. John's grade = (58+50)/2 = **54/60**.

---

## UC-30: Set Up an Instructor Account

**Actor:** Instructor | **Priority:** High | **Frequency:** ~2×/year

**Trigger:** Instructor clicks registration link in invitation email.

**Details to provide:** First name, middle initial (optional), last name, password, confirm password.

---

## UC-31: Generate Section Peer Evaluation Report

**Actor:** Instructor | **Priority:** High | **Frequency:** ~1×/week

**Report parameters:** Active week (default: previous week).

**Report columns:** Student name, grade, public comments, private comments, commenter.

**Report also shows:** Which students did not submit a peer evaluation that week.

**Grade algorithm:** See UC-29.

**Drill-down:** Instructor may view the full breakdown of scores given by each evaluator for any student.

---

## UC-32: Generate Team WAR Report

**Actors:** Instructor, Student | **Priority:** High | **Frequency:** ~1×/week

**Report parameters:** Active week (default: previous week).

**Report columns:** Student name, activity category, planned activity, description, planned hours, actual hours, status.

**Sorted:** Last name ascending by default.

**Report also shows:** Which students did not submit a WAR that week.

---

## UC-33: Generate Student Peer Evaluation Report

**Actor:** Instructor | **Priority:** High | **Frequency:** ~10×/week

**Accessed via:** UC-16 (View a student).

**Report parameters:** Start active week and end active week.

**Report columns:** Week, grade, public comments, private comments, commenter — sorted chronologically.

---

## UC-34: Generate Student WAR Report

**Actor:** Instructor | **Priority:** High | **Frequency:** ~10×/week

**Accessed via:** UC-16 (View a student).

**Report parameters:** Start active week and end active week.

**Report format:** Grouped by active week, sorted chronologically.

**Columns per week:** Activity category, planned activity, description, planned hours, actual hours, status.

---

## Business Rules

| ID | Rule |
|---|---|
| BR-1 | Every team must have at least one instructor. An instructor can supervise multiple teams. |
| BR-2 | Active weeks for fall: weeks 5–15. Spring: weeks 1–15. Winter holiday = inactive. Students may only submit peer evaluations during active weeks, but can submit WAR activities at any time. |
| BR-3 | Peer evaluations cannot be edited once submitted. |
| BR-4 | A student can only submit a peer evaluation for the **previous** week. They have one week to do so; late submissions are not accepted. |
| BR-5 | A student can only see their own rubric criterion scores, public comments, and overall grade — never the evaluators' identities or private comments. |
