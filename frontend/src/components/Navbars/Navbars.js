import React, {Component} from 'react';
import {
    Navbar,
    Nav,
    Offcanvas
} from 'react-bootstrap';
import Container from 'react-bootstrap/Container'
import UserNavbar from './UserNavbar';

import './Navbars.css'

class Navbars extends Component {
    render() {
        return (
            <Navbar key='lg' bg='dark' expand='lg' className='mb-3 site-header'>
                <Container fluid>
                    <Navbar.Toggle aria-controls='offcanvasNavbar-expand-lg'/>
                    <Navbar.Offcanvas
                        id='offcanvasNavbar-expand-lg'
                        aria-labelledby='offcanvasNavbarLabel-expand-lg'
                        placement='end'>
                        <Offcanvas.Header closeButton>
                            <Offcanvas.Title id='offcanvasNavbarLabel-expand-lg'>
                                Menu
                            </Offcanvas.Title>
                        </Offcanvas.Header>
                        <Offcanvas.Body className='container nabvar-left-space-mobile'>
                            <Nav className='justify-content-first flex-grow-1 pe-3'>
                                <Nav.Link href='/'>Home</Nav.Link>
                                <Nav.Link href='/business'>Business</Nav.Link>
                                <Nav.Link href='/health'>Health</Nav.Link>
                                <Nav.Link href='/entertainment'>Entertainment</Nav.Link>
                                <Nav.Link href='/technology'>Technology</Nav.Link>
                                <Nav.Link href='/science'>Science</Nav.Link>
                                <Nav.Link href='/sports'>Sports</Nav.Link>
                            </Nav>
                            <UserNavbar/>
                        </Offcanvas.Body>
                    </Navbar.Offcanvas>
                </Container>
            </Navbar>
        )
    };
}

export default Navbars;