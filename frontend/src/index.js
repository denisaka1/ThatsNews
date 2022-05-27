import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import rootReducer from './reducers/RootReducer';
import {createStore} from 'redux';
import {Provider} from 'react-redux';
import {loadState, saveState} from './store/localStorage';

import '../node_modules/bootstrap/dist/css/bootstrap.min.css'
import '../node_modules/font-awesome/css/font-awesome.min.css'
import './assets/dist/css/stylesheet.css';

const persistedState = loadState();

// create kind of store with root reducer and the restore is we have one
const store = createStore(rootReducer, persistedState);
// console.log(rootReducer.getState);

// saving the rootreducer to the local storage
saveState(rootReducer.getState);

store.subscribe(() => {
    saveState(store.getState());
});

ReactDOM.render(
    <React.StrictMode>
        <Provider store={store}>
            <App/>
        </Provider>
    </React.StrictMode>,
    document.getElementById('root')
);