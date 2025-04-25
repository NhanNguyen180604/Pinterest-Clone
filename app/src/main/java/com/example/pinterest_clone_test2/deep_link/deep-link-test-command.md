## How to test deep link
Since android emulator's browser is a joke, use the adb command below to test deep link.

Change pinId to whatever you like.

## Command for pin sharing
```
adb shell am start -W -a android.intent.action.VIEW -d "https://open.my.pinterest-clone/shared-pin/?pinId=dDPGQAM3Eox9X8XrJiV4" com.example.pinterest_clone_test2
```

## Command for inviting board collaborator
```
adb shell am start -W -a android.intent.action.VIEW -d "https://open.my.pinterest-clone/add-collaborators/?boardId=6g4XkATx3GZYgw6mdTzp\&userId=3BleBo6wwIQd7JOgHxuLy1aWDMe2" com.example.pinterest_clone_test2
```

## Have no adb?
In case you are like me, not having the fucking adb command, lemme kindly teach you, cuz fuck android
- Go to your sdk installation folder, the AndroidSDK folder
- Go to the "platform-tools" directory, copy the path
- Go to environment variables, edit the Path variable, add the copied path
- Open powershell, type adb to check if it is recognized
- Mac? I don't use that thing, figure it out yourself