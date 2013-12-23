#ifndef _SEVEN_SEGMENT_H_
#define _SEVEN_SEGMENT_H_

#define SS_BORDER 5
#define SS_EDGE 15

#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

namespace sevensegment {
    int getWidth (char c) {
        switch(c) {
            case '1':
                return SS_BORDER;
            default:
                return SS_EDGE;
        }
    }

    void draw (Mat& img, char c, Point tl, const Scalar& color, int size=1) {
        int thickness = size * SS_BORDER;
        switch(c) {
            case '0': {
                line(img, tl, Point(tl.x + SS_EDGE, tl.y), color, thickness);
                line(img, tl, Point(tl.x, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x, tl.y + SS_EDGE), Point(tl.x, tl.y + SS_EDGE * 2), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y), Point(tl.x + SS_EDGE, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y + SS_EDGE), Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), color, thickness);
                line(img, Point(tl.x, tl.y + SS_EDGE * 2), Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), color, thickness);
            }
            case '1': {
                line(img, tl, Point(tl.x, tl.y + SS_EDGE * 2), color, thickness);
                break;
            }
            case '3': {
                line(img, tl, Point(tl.x + SS_EDGE, tl.y), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y), Point(tl.x + SS_EDGE, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x, tl.y + SS_EDGE), Point(tl.x + SS_EDGE, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y + SS_EDGE), Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), color, thickness);
                line(img, Point(tl.x, tl.y + SS_EDGE * 2), Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), color, thickness);
                break;
            }
            case '5': {
                line(img, tl, Point(tl.x + SS_EDGE, tl.y), color, thickness);
                line(img, tl, Point(tl.x, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x, tl.y + SS_EDGE), Point(tl.x + SS_EDGE, tl.y + SS_EDGE), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y + SS_EDGE), Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), color, thickness);
                line(img, Point(tl.x + SS_EDGE, tl.y + SS_EDGE * 2), Point(tl.x, tl.y + SS_EDGE * 2), color, thickness);
                break;
            }
            default: {
                std::cerr << "[Warning] Unsupported character: " << c << endl;
            }
        }
    }

    void drawString (Mat& img, std::string s, Point tl, const Scalar& color, int size=1) {
        int x = tl.x;
        int y = tl.y;
        for (int i=0; i<s.length(); i++) {
            char c = s[i];
            draw(img, c, Point (x, y), color, size);
            x += getWidth(c) + SS_BORDER * 2;
        }
    }
}

#endif
