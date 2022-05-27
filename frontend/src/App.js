import React from 'react'
import {
    BrowserRouter as Router,
    Routes,
    Route
} from 'react-router-dom';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import Default from './layouts/Default';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import Favorites from './layouts/Favorite';

function App() {
    document.title = "That's News";
    return (
        <div>
            <Header/>
            <main className='container py-4'>
                <Router>
                    <Routes>
                        <Route exact path='/' element={<Default/>}/>
                        <Route path='/login' element={<Login/>}/>
                        <Route path='/register' element={<Register/>}/>
                        <Route exact path='/business' element={<Default category='business'/>}/>
                        <Route exact path='/entertainment' element={<Default category='entertainment'/>}/>
                        <Route exact path='/health' element={<Default category='health'/>}/>
                        <Route exact path='/science' element={<Default category='science'/>}/>
                        <Route exact path='/sports' element={<Default category='sports'/>}/>
                        <Route exact path='/technology' element={<Default category='technology'/>}/>
                        <Route exact path='/favorites' element={<Favorites/>}/>
                    </Routes>
                </Router>
                <Footer/>
            </main>
        </div>
    );
}

export default App;
