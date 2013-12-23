#include <opencv2/opencv.hpp>
#include "Scouter.hpp"

#define WINDOW_NAME "Scouter"

using namespace cv;

int main () {

	Scouter* scouter = new Scouter("../res/raw/haarcascade_frontalface_default.xml", "../res/raw/haarcascade_mcs_nose.xml");
    VideoCapture capture(0);
    capture.set(CV_CAP_PROP_FRAME_WIDTH, 480);
    capture.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
    Mat frame, rgba;
    while (1) {
        capture >> frame;
        cvtColor(frame, rgba, CV_BGR2RGBA);
        scouter->process(rgba);
        imshow(WINDOW_NAME, rgba);
        char c = waitKey(2);
        if (c == '\x1b') {
            break;
        }
	}
	return 0;
}
