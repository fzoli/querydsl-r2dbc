rootProject.name = "querydsl-r2dbc"

fun module(group: String, name: String) {
    include(name)
    project(":$name").projectDir = file("modules/$group/$name")
}

// ---------------------------

module("lib", "querydsl-r2dbc")
