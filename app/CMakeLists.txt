cmake_minimum_required(VERSION 3.8)

# Enable C++11
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

project(phonebotroot CXX)

add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/app/jni/" ${CMAKE_CURRENT_BINARY_DIR}/opencv)
add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/../../libphonebot/" ${CMAKE_CURRENT_BINARY_DIR}/libphonebot)