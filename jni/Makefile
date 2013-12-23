ifeq ($(strip $(OPENCV_HOME)),)
#default OPENCV_HOME
OPENCV_HOME=/usr/local/opencv-2.4.1
endif

CXX=g++
CXXFLAGS=-I$(OPENCV_HOME)/include -g
LDFLAGS=-L${OPENCV_HOME}/lib -lopencv_core -lopencv_highgui -lopencv_features2d -lopencv_imgproc -lopencv_calib3d -lopencv_objdetect
BUILD_DIR=bin
SRCS= \
	Main.cpp Scouter.cpp
OBJS=${SRCS:%.cpp=${BUILD_DIR}/%.o}

scouter: $(OBJS)
	${CXX} ${LDFLAGS} ${OBJS} -o $@
${BUILD_DIR}/%.o : %.cpp
	@mkdir -p $(dir $@)
	${CXX} ${CXXFLAGS} -c $< -o $@
clean:
	rm -rf bin scouter
