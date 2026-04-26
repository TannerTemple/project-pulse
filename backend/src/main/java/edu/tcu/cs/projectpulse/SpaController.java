package edu.tcu.cs.projectpulse;

// Intentionally empty — SPA routing is handled by SpaFilter.
// The {*path} catch-all was removed because it intercepted static asset requests
// (/assets/*.js, /assets/*.css) before Spring's ResourceHttpRequestHandler could
// serve them, causing those files to 404 and Vue to never load.
