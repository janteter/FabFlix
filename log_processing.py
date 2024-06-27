import os
import sys

def processLog(logFileList):
    totalLines = 0
    tsTotalTime = 0
    tjTotalTime = 0

    for filePath in logFileList:
        try:
            with open(filePath, "r") as logFile:
                for line in logFile:
                    lineContents = line.split()
                    if lineContents[0] == "TS" and lineContents[2] == "TJ":
                        tsTotalTime += int(lineContents[1])
                        tjTotalTime += int(lineContents[3])
                        totalLines += 1
        except FileNotFoundError as fne:
            print("File could not be found!")
    print("TS", tsTotalTime / totalLines / 1_000_000, "ms")
    print("TJ", tjTotalTime / totalLines / 1_000_000, "ms")
    return tsTotalTime / totalLines / 1_000_000, tjTotalTime / totalLines / 1_000_000

if __name__ == "__main__":
    if len(sys.argv) >= 2:
        fileNames = sys.argv[1:]
        processLog(fileNames)
    else:
        raise RuntimeError("Did not recieve correct number of params (need at least 2), got", len(sys.argv), "param(s) instead")