import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:image_face/image_face.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _displayMsg = '';
  File _image;

  @override
  void initState() {
    super.initState();
    // initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> pickFace() async {
    String displayMsg = 'loading';
    File pickedFile = await ImagePicker.pickImage(source: ImageSource.gallery);
    setState(() {
      _displayMsg = displayMsg;
      _image = pickedFile;
    });

    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      print('start check');
      int value = await ImageFace.numberOfFaces(pickedFile);
      print('end check');
      displayMsg = 'number of faces: $value';
    } on PlatformException {
      displayMsg = 'Failed to get faces';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _displayMsg = displayMsg;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                const SizedBox(height: 50),
                Text('$_displayMsg\n'),
                const SizedBox(height: 50),
                TextButton(child: Text('Select image'), onPressed: pickFace),
                const SizedBox(height: 50),
                if (_image != null) Image.file(_image)
              ],
            ),
          ),
        ),
      ),
    );
  }
}
