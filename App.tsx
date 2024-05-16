/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import firebase from '@react-native-firebase/app';
import React, {useEffect, useState} from 'react';
import {
  StyleSheet,
  Text,
  View,
  NativeModules,
  Button,
  PermissionsAndroid,
  Image,
  Modal,
} from 'react-native';
import analytics from '@react-native-firebase/analytics';
import remoteConfig from '@react-native-firebase/remote-config';

const {CalendarModule} = NativeModules;

console.log(CalendarModule);

function App(): React.JSX.Element {
  const [state, setState] = useState();
  const getPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
        {
          title: 'Cool Photo App Camera Permission',
          message:
            'Cool Photo App needs access to your camera ' +
            'so you can take awesome pictures.',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('You can READ_EXTERNAL_STORAGE');
      } else {
        console.log('Camera permission denied');
      }
    } catch (err) {
      console.warn(err);
    }

    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        {
          title: 'Cool Photo App Camera Permission',
          message:
            'Cool Photo App needs access to your camera ' +
            'so you can take awesome pictures.',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('You can WRITE_EXTERNAL_STORAGE');
      } else {
        console.log('Camera permission denied');
      }
    } catch (err) {
      console.warn(err);
    }
  };

  useEffect(() => {

    getPermission();
  }, []);

  const logEvent = async () => {
    await analytics().logSignUp({
      method: 'facebook',
    });
  };

  return (
    <>
      <View style={styles.container}>
        <View
          style={{
            backgroundColor: 'orange',
            flexDirection: 'column',
            gap: 40,
            padding: 20,
          }}>
          <Text style={{fontWeight: '800',fontSize: 45, color: 'lightgrey'}}>
            This is from React Native bundle 7
          </Text>
        </View>
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 20,
    backgroundColor: 'skyblue',
  },
  image: {
    width: '100%',
    height: 200,
  },
});

export default App;
