import sys

def func0(intval):
    if intval in [1, 2, 5, 6]:
        return intval + 1
    return intval

if __name__ == "__main__":
    args = sys.argv[1:]
    new_args = [eval(arg) for arg in args]
    print (func0(*new_args))