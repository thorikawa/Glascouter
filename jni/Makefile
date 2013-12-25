ifeq ($(strip $(OPENCV_HOME)),)
#default OPENCV_HOME
OPENCV_HOME=/usr/local/opencv-2.4.1
endif

CXX=g++
CXXFLAGS=-I$(OPENCV_HOME)/include -g
LDFLAGS=-L${OPENCV_HOME}/lib -lopencv_core -lopencv_highgui -lopencv_features2d -lopencv_imgproc -lopencv_calib3d -lopencv_objdetect -lopencv_contrib
BUILD_DIR=bin
SRCS= \
	Main.cpp Scouter.cpp
OBJS=${SRCS:%.cpp=${BUILD_DIR}/%.o}
LEARN_SRCS = \
	learn.cpp preprocessFace.cpp detectObject.cpp
LEARN_OBJS=${LEARN_SRCS:%.cpp=${BUILD_DIR}/%.o}

scouter: $(OBJS)
	${CXX} ${LDFLAGS} ${OBJS} -o $@
${BUILD_DIR}/%.o : %.cpp
	@mkdir -p $(dir $@)
	${CXX} ${CXXFLAGS} -c $< -o $@
learn: ${LEARN_OBJS}
	${CXX} ${LDFLAGS} ${LEARN_OBJS} -o $@
clean:
	rm -rf ${BUILD_DIR} scouter learn
