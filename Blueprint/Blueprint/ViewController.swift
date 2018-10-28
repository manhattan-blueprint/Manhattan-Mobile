//
//  ViewController.swift
//  Blueprint
//
//  Created by Jay Lees on 18/10/2018.
//  Copyright © 2018 Manhattan. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var usernameInput: UITextField!
    @IBOutlet weak var passwordInput: UITextField!
    @IBOutlet weak var loginButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        usernameInput.placeholder = "Username"
        passwordInput.placeholder = "Password"
        
        loginButton.setTitle("Login", for: .normal)
        loginButton.layer.cornerRadius = 4
    }
    
    @IBAction func userTappedLogin(_ sender: UIButton) {
        loginButton.isUserInteractionEnabled = false
        loginButton.setTitleColor(UIColor.green, for: .disabled)
            
        print(usernameInput.text ?? "User instance is null")
        print(passwordInput.text ?? "pass instance is null")
    }
    
}

