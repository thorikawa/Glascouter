#include <opencv2/opencv.hpp>
#include <fstream>
#include "Scouter.hpp"
#include "preprocessFace.h"

#define WINDOW_NAME "Scouter"

using namespace std;
using namespace cv;

typedef std::pair<double, int> mypair;
bool comparator(const mypair& l, const mypair& r) {
	return l.first < r.first;
}

static void read_csv(const string& filename, vector<Mat>& images,
		vector<int>& labels, char separator = ';') {
	ifstream file(filename.c_str());
	if (!file) {
		string error_message =
				"No valid input file was given, please check the given filename.";
		CV_Error(CV_StsBadArg, error_message);
	}
	string line, path, classlabel;
	while (getline(file, line)) {
		stringstream liness(line);
		getline(liness, path, separator);
		getline(liness, classlabel);
		if (!path.empty() && !classlabel.empty()) {
			images.push_back(imread(path, 0));
			labels.push_back(atoi(classlabel.c_str()));
		}
	}
}

const char *facerecAlgorithm = "FaceRecognizer.Fisherfaces";
//const char *facerecAlgorithm = "FaceRecognizer.Eigenfaces";
const bool preprocessLeftAndRightSeparately = true;

// command line tool to learn face datas and save the recognizer object
int main() {
	vector < Mat > faces;
	vector<int> labels;
	read_csv("at.txt", faces, labels);
	Ptr < FaceRecognizer > model;
	model = Algorithm::create < FaceRecognizer > (facerecAlgorithm);
	if (model.empty()) {
		cerr << "ERROR: The FaceRecognizer algorithm [" << facerecAlgorithm
				<< "] is not available in your version of OpenCV. Please update to OpenCV v2.4.1 or newer."
				<< endl;
		exit(1);
	}
	cout << "start training" << endl;
	// model->train(faces, labels);
	cout << "end training" << endl;
	// model->save("model.data");
	model->load("model.data");
	cout << "save model" << endl;

	// TEST!
	CascadeClassifier faceCascade;
	faceCascade.load("../res/raw/haarcascade_frontalface_default.xml");

	CascadeClassifier eyeCascade1;
	eyeCascade1.load("../res/raw/haarcascade_eye.xml");

	CascadeClassifier eyeCascade2;
	eyeCascade1.load("../res/raw/haarcascade_eye.xml");

	// Mat test = faces[10];
	Mat aa = faces[0];
	cout << aa.cols << endl;
	cout << aa.rows << endl;
	Mat color = imread("takahiro2.jpg");
	// Mat test;
	// cvtColor(color, test, CV_RGB2GRAY);
	// resize(test, test, Size(112, 92));
    Rect faceRect;  // Position of detected face.
    Rect searchedLeftEye, searchedRightEye; // top-left and top-right regions of the face, where eyes were searched.
    Point leftEye, rightEye;    // Position of the detected eyes.
    Mat test = getPreprocessedFace(color, 92, 112, faceCascade, eyeCascade1, eyeCascade2, preprocessLeftAndRightSeparately, &faceRect, &leftEye, &rightEye, &searchedLeftEye, &searchedRightEye);
	cout << test.cols << endl;
	cout << test.rows << endl;
	cout << test.channels() << endl;
	Mat eigenvectors = model->get < Mat > ("eigenvectors");
	Mat averageFaceRow = model->get < Mat > ("mean");
	// Project the input image onto the PCA subspace.
	Mat projection = subspaceProject(eigenvectors, averageFaceRow,
			test.reshape(1, 1));
	int predict = model->predict(test);
	cout << predict << endl;
	cout << projection << endl;
	cout << projection.rows << "," << projection.cols << endl;
	vector<mypair> vec;
	for (int i = 0; i < projection.cols; i++) {
		double a = projection.at<double>(0, i);
		vec.push_back(mypair(a, i));
	}
	sort(vec.begin(), vec.end(), comparator);
	for (int i = 0; i < projection.cols; i++) {
		cout << vec[i].first << endl;
	}

	return 0;
}
