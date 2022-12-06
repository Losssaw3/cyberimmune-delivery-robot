rootProject.name = "delivery"
include(
    "libs:crypto",
    "libs:messages"
)
include("monitor")
include(
    "backend:flightmanager",
    "backend:authentication",
    "backend:store"
)
include(
    "robot:hmi"
)
