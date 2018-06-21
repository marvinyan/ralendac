# Mobile App

## Required

- [ ] The UI should have one month hard coded view (Pick any month)
- [x] Ignore users/login, just have one hardcoded user
- [ ] Click on a day box, and be able to create a new event on that day which gets sent to the backend on clicking submit.
  - [x] The form should have start time, end time, description and submit.
  - [x] Once submit is clicked the form should disappear.
  - [ ] Event should now appear in that day’s box.
  - [x] Events cannot span multiple days. Must start and end the same day.
- [ ] Show all events the user has on their calendar.
- [ ] The UI should have 4 rows of 7 boxes (simple case of a 28 day month).
- [x] The application should communicate with an API backend using JSON. Don’t spend a lot of time on the UI making it look beautiful; just make it functional.

## Optional

- [ ] Switch between months
- [ ] Week or day view
- [ ] Handle events spanning multiple days
- [ ] Handle too many events to fit in your box UI on a given day.
- [x] You should be able to update/delete events. How you implement this UX is up to you.
- [ ] The UI should have 5 rows of 7 boxes with the correct date on the correct days.

# Back End

## Required

- [x] POST /events
- [x] GET /events

## Optional

- [x] DELETE /events/:id
- [x] PUT /events/:id
