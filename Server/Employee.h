#include <iostream>
#include <cstdlib>
#include <string>
using namespace std;


class Employee {
public:
	Employee(string theName, float thePayRate);

	string getName() const;
	float getPayRate() const;

	float pay(float hoursWorked) const;

protected:
	string name;
	float payRate;
};

