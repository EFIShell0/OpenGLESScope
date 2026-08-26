# OpenGLESScope 0.7.0 external OpenGL ES capability reference public capability-floor audit

The checked-in `PUBLIC_CAPABILITY_REFERENCE_MATRIX.csv` is the auditable OpenGL ES public-database capability floor used by the project.

- Public external comparison reference core capability floor: 145 rows.
- OpenGLESScope coverage: 145/145.
- Additional OpenGLESScope implementation queries: 49.
- Every tracked row has an in-app/TXT/HTML/Database path.
- The release verifier requires every tracked capability token to remain present in the native collector.

The parity target is graphics capability inspection. CPU inventory, sensors, generic Android feature inventory and legacy OpenGL ES 1.x are not copied merely to inflate counts because they are outside OpenGLESScope's stated graphics scope.

0.7.0 also removes two values that must not be represented as capability limits: `GL_NUM_WINDOW_RECTANGLES_EXT` is mutable state and `GL_MAX_SHADER_COMPILER_THREADS_KHR` is the KHR parallel-compile control/hint state rather than a reported implementation maximum.
