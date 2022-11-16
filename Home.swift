//
//  Home.swift
//  Project
//
//  Created by Abu Nabe on 25/7/21.
//

import UIKit
import GoogleMaps
import GooglePlaces
import CoreLocation
import Alamofire

class Home: UIViewController
{
    //    @IBOutlet var mapView: GMSMapView!
    let URL_Age = url_connect.url + "Age.php"
    
    var mapType = "get_location_private"
    var connected = "false"
    var privacy = "yes"
    
    var func1 = "no"
    let currentUser = UserDefaults.standard.getUsername()
    let locationManager = CLLocationManager()
    
    var zoom: Float = 15
    var mapAge = String()
    
    
    
    let mapView: GMSMapView = {
        let map = GMSMapView()
        
        return map
    }()
    
    private let backLabel: UILabel = {
        let Register = UILabel()
        Register.text = "Map"
        Register.textColor = UIColor(named: "Basic")
        Register.font = .boldSystemFont(ofSize: 16.0)
        Register.sizeToFit()
        return Register
    }()
    
    private let navBarView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "Basic2")
        
        return view
    }()
    
    private let rightImageView: UIImageView = {
        let imageview = UIImageView()
        imageview.image = UIImage(systemName: "flame.fill")?.withRenderingMode(.alwaysOriginal).withTintColor(UIColor(named: "Basic")!)
        imageview.contentMode = UIView.ContentMode.scaleAspectFit
        
        return imageview
    }()
    
    let toolbarShadow: UIView = {
        let line = UIView()
        line.backgroundColor = .lightGray
        line.layer.masksToBounds = false
        line.layer.shadowColor = UIColor.lightGray.cgColor
        line.layer.shadowOpacity = 0.8
        line.layer.shadowOffset = CGSize(width: 0, height: 1.0)
        line.layer.shadowRadius = 2
        return line
    }()
    
    let ChatIcon: UIImageView = {
        let Label = UIImageView()
        Label.image = UIImage(systemName: "message.fill")
        Label.tintColor = UIColor(named: "Purple")
        return Label
    }()
    
    let ChatLabel: UILabel = {
        let Label = UILabel()
        Label.textAlignment = .center
        Label.text = "chat".localized()
        Label.textColor = UIColor(named: "Purple")
        Label.font = .boldSystemFont(ofSize: 12)
        return Label
    }()
    
    let RankIcon: UIImageView = {
        let Label = UIImageView()
        Label.image = UIImage(named: "RankIcon")?.withRenderingMode(.alwaysOriginal).withTintColor( UIColor(named: "Purple")!)
        return Label
    }()
    
    let RankLabel: UILabel = {
        let Label = UILabel()
        Label.textAlignment = .center
        Label.text = "rank".localized()
        Label.textColor = UIColor(named: "Purple")
        Label.font = .boldSystemFont(ofSize: 12)
        return Label
    }()
    
    let RelocateIcon: UIImageView = {
        let Label = UIImageView()
        Label.image = UIImage(systemName: "mappin.circle")
        Label.tintColor = UIColor(named: "Purple")
        return Label
    }()
    
    let RelocateLabel: UILabel = {
        let Label = UILabel()
        Label.textAlignment = .center
        Label.text = "relocate".localized()
        Label.textColor = UIColor(named: "Purple")
        Label.font = .boldSystemFont(ofSize: 12)
        return Label
    }()
    
    
    let ConnectIcon: UIImageView = {
        let Label = UIImageView()
        Label.image = UIImage(systemName: "map.fill")
        Label.tintColor = UIColor(named: "Purple")
        return Label
    }()
    
    let ConnectLabel: UILabel = {
        let Label = UILabel()
        Label.textAlignment = .center
        Label.text = "connect".localized()
        Label.textColor = UIColor(named: "Purple")
        Label.font = .boldSystemFont(ofSize: 12)
        return Label
    }()
    
    private let centerView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "Basic")
        
        return view
    }()
    
    private let centerView2: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "Basic")
        
        return view
    }()
    
    private let zoomIn: UIImageView = {
        let imageview = UIImageView()
        imageview.image = UIImage(systemName: "plus.app")?.withRenderingMode(.alwaysOriginal).withTintColor(UIColor(named: "Purple")!)
        imageview.contentMode = UIView.ContentMode.scaleAspectFit
        imageview.isHidden = true
        return imageview
    }()
    
    private let zoomOut: UIImageView = {
        let imageview = UIImageView()
        imageview.image = UIImage(systemName: "minus.square")?.withRenderingMode(.alwaysOriginal).withTintColor(UIColor(named: "Purple")!)
        imageview.contentMode = UIView.ContentMode.scaleAspectFit
        imageview.isHidden = true
        return imageview
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(navBarView)
        view.addSubview(toolbarShadow)
        
        self.navigationController?.navigationBar.isHidden = true
        
        let height = navigationController!.navigationBar.frame.size.height
        navBarView.anchor(top: view.safeAreaLayoutGuide.topAnchor, left: view.leftAnchor, right: view.rightAnchor, height: height)
        
        toolbarShadow.anchor(top: navBarView.bottomAnchor, width: view.width, height: 1)
        
        configureNav()
        
        view.addSubview(mapView)
        
        view.addSubview(centerView)
        view.addSubview(centerView2)
        view.addSubview(ChatIcon)
        view.addSubview(RankIcon)
        view.addSubview(ConnectIcon)
        view.addSubview(RelocateIcon)
        view.addSubview(ChatLabel)
        view.addSubview(RankLabel)
        view.addSubview(ConnectLabel)
        view.addSubview(RelocateLabel)
        view.addSubview(zoomIn)
        view.addSubview(zoomOut)
        
        self.navigationController?.navigationBar.isHidden = true
        
        centerView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        centerView.anchor(paddingTop: 0)
        
        centerView2.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        centerView2.anchor(paddingTop: 0)
        locationManager.delegate = self
        
        RelocateIcon.anchor(top: toolbarShadow.bottomAnchor, right: centerView.leftAnchor, paddingTop: 5, paddingRight: 15, width: 30, height: 30)
        
        RelocateLabel.centerXAnchor.constraint(equalTo: RelocateIcon.centerXAnchor).isActive = true
        RelocateLabel.anchor(top: RelocateIcon.bottomAnchor)
        
        ConnectIcon.anchor(top: toolbarShadow.bottomAnchor, left: centerView.rightAnchor, paddingTop: 5, paddingLeft: 15, width: 30, height: 30)
        
        ConnectLabel.centerXAnchor.constraint(equalTo: ConnectIcon.centerXAnchor).isActive = true
        ConnectLabel.anchor(top: ConnectIcon.bottomAnchor)
        
        ChatIcon.anchor(top: toolbarShadow.bottomAnchor, right: RelocateIcon.leftAnchor, paddingTop: 5, paddingRight: 25, width: 30, height: 30)
        
        ChatLabel.centerXAnchor.constraint(equalTo: ChatIcon.centerXAnchor).isActive = true
        ChatLabel.anchor(top: ChatIcon.bottomAnchor)
        
        RankIcon.anchor(top: toolbarShadow.bottomAnchor, left: ConnectIcon.rightAnchor, paddingTop: 5, paddingLeft: 25, width: 30, height: 30)
        
        RankLabel.centerXAnchor.constraint(equalTo: RankIcon.centerXAnchor).isActive = true
        RankLabel.anchor(top: RankIcon.bottomAnchor)
        
        mapView.anchor(top: toolbarShadow.bottomAnchor, left: view.leftAnchor,bottom: view.safeAreaLayoutGuide.bottomAnchor, right: view.rightAnchor)
        
        zoomIn.anchor(bottom: centerView2.topAnchor, right: view.rightAnchor, paddingBottom: 5, paddingRight: 10, width: 30, height: 30)
        
        zoomOut.anchor(top: centerView2.bottomAnchor, right: view.rightAnchor, paddingTop: 5, paddingRight: 10, width: 30, height: 30)
        
        
        mapView.delegate = self
        
        if self.traitCollection.userInterfaceStyle == .dark {
            do {
                if let styleURL = Bundle.main.url(forResource: "night_mode", withExtension: "json") {
                    mapView.mapStyle = try GMSMapStyle(contentsOfFileURL: styleURL)
                } else {
                    print("XAS", "Unable to find style.json")
                }
            } catch {
                print("XAS","One or more of the map styles failed to load. \(error)")
            }
            
        }
        
        
        let relocateTap = UITapGestureRecognizer(target: self, action: #selector(relocateLocation))
        RelocateIcon.isUserInteractionEnabled = true
        RelocateIcon.addGestureRecognizer(relocateTap)
        
        let connectTap = UITapGestureRecognizer(target: self, action: #selector(changeLocationType))
        ConnectIcon.isUserInteractionEnabled = true
        ConnectIcon.addGestureRecognizer(connectTap)
        
        let rankTap = UITapGestureRecognizer(target: self, action: #selector(GoToLeaderboard))
        RankIcon.isUserInteractionEnabled = true
        RankIcon.addGestureRecognizer(rankTap)
        
        let chatTap = UITapGestureRecognizer(target: self, action: #selector(GoToChat))
        ChatIcon.isUserInteractionEnabled = true
        ChatIcon.addGestureRecognizer(chatTap)
        
        let zoomInTap = UITapGestureRecognizer(target: self, action: #selector(ZoomIn))
        zoomIn.isUserInteractionEnabled = true
        zoomIn.addGestureRecognizer(zoomInTap)
        
        let zoomOutTap = UITapGestureRecognizer(target: self, action: #selector(ZoomOut))
        zoomOut.isUserInteractionEnabled = true
        zoomOut.addGestureRecognizer(zoomOutTap)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        self.tabBarController?.tabBar.isHidden = false
    }
    
    @objc func ZoomIn(_ sender: UIButton) {
        let nextZoom = zoom + 1
        mapView.animate(toZoom: nextZoom)
    }
    
    @objc func ZoomOut(_ sender: UIButton) {
        let nextZoom = zoom - 1
        mapView.animate(toZoom: nextZoom)
    }
    
    func configureNav()
    {
        navBarView.addSubview(backLabel)
        navBarView.addSubview(rightImageView)
        
        backLabel.centerYAnchor.constraint(equalTo: navBarView.centerYAnchor).isActive = true
        backLabel.anchor(left: navBarView.leftAnchor, paddingLeft: 20)
        
        rightImageView.centerYAnchor.constraint(equalTo: navBarView.centerYAnchor).isActive = true
        rightImageView.anchor(right: navBarView.rightAnchor, paddingRight: 5, width: 30, height: 30)
        
        let rightTap = UITapGestureRecognizer(target: self, action: #selector(GoToVideoWeekly))
        rightImageView.isUserInteractionEnabled = true
        rightImageView.addGestureRecognizer(rightTap)
        
    }
    
    @objc func GoToVideoWeekly()
    {
        let storyboard = UIStoryboard(name: "MapList", bundle: nil)
        let secondViewController = storyboard.instantiateViewController(withIdentifier: "MapList") as! MapList
        self.navigationController?.pushViewController(secondViewController, animated: true)
    }
    
    
    @objc func GoToChat()
    {
        let storyboard = UIStoryboard(name: "MapChat", bundle: nil)
        let secondViewController = storyboard.instantiateViewController(withIdentifier: "MapChat") as! MapChat
        self.navigationController?.present(secondViewController, animated: true)
    }
    
    @objc func goToProfile(username: String, profileImage: String)
    {
        let storyboard = UIStoryboard(name: "FriendProfile", bundle: nil)
        let secondViewController = storyboard.instantiateViewController(withIdentifier: "FriendProfile") as! FriendProfile
        secondViewController.NameLabel.text = username
        secondViewController.visitProfilePic = profileImage
        self.navigationController?.pushViewController(secondViewController, animated: true)
    }
    
    @objc func goToMessages(username: String, profileImage: String, unreadCount: String){
        
        let storyBoard: UIStoryboard = UIStoryboard(name: "Chat", bundle: nil)
        let newViewController = storyBoard.instantiateViewController(withIdentifier: "Chat") as! Chat
        newViewController.receiverName = username
        newViewController.unreadCount = unreadCount
        newViewController.receiverProfilePic = profileImage
        self.hidesBottomBarWhenPushed = true
        self.navigationController?.pushViewController(newViewController, animated: false)
        
    }
    
    @objc func GoToLeaderboard()
    {
        let storyboard = UIStoryboard(name: "LeaderboardPager", bundle: nil)
        let secondViewController = storyboard.instantiateViewController(withIdentifier: "LeaderboardPager") as! LeaderboardPager
        secondViewController.hidesBottomBarWhenPushed = true
        self.navigationController?.pushViewController(secondViewController, animated: false)
    }
    
    @objc func relocateLocation()
    {
        mapView.clear()
        let authorizationStatus: CLAuthorizationStatus
        
        if #available(iOS 14, *) {
            authorizationStatus = locationManager.authorizationStatus
        } else {
            authorizationStatus = CLLocationManager.authorizationStatus()
        }
        
        if CLLocationManager.locationServicesEnabled() {
            
            
            switch authorizationStatus {
            case .notDetermined:
                // Request when-in-use authorization initially
                // This is the first and the ONLY time you will be able to ask the user for permission
                self.locationManager.delegate = self
                locationManager.requestWhenInUseAuthorization()
                break
                
            case .restricted, .denied:
                // Disable location features
                PermissionAlert.MapPermission(navigationController: navigationController!)
                
                break
                
            case .authorizedWhenInUse, .authorizedAlways:
                // Enable features that require location services here.
                print("Full Access")
                locationManager.requestLocation()
                locationManager.startUpdatingLocation()
                
                //5
                mapView.isMyLocationEnabled = true
                mapView.settings.myLocationButton = true
                
                break
            }
        }
    }
    
    
    @objc func changeLocationType(){
        if(func1 == "no"){
            func1 = "yes"
            mapView.clear()
            let authorizationStatus: CLAuthorizationStatus
            
            if #available(iOS 14, *) {
                authorizationStatus = locationManager.authorizationStatus
            } else {
                authorizationStatus = CLLocationManager.authorizationStatus()
            }
            
            if CLLocationManager.locationServicesEnabled() {
                switch authorizationStatus {
                case .notDetermined:
                    // Request when-in-use authorization initially
                    // This is the first and the ONLY time you will be able to ask the user for permission
                    
                    if(connected == "false"){
                        let alert = UIAlertController(title: "Connect", message: "You and other users who are currently connected will be shown on the map.", preferredStyle: .alert)
                        
                        alert.addAction(UIAlertAction(title: "Global", style: .default , handler:{ [self] (UIAlertAction)in
                            
                            
                            let parameters: Parameters=[
                                "type": "get",
                                "username": currentUser,
                                
                            ]
                            
                            AF.request(URL_Age, method: .post, parameters: parameters).responseString
                            { [self]
                                response in
                                switch response.result {
                                case let .success(value):
                                    if let JSON = value as? [String: Any]
                                    {
                                        let day = JSON["day"] as? String ?? "23"
                                        let month = JSON["month"] as? String ?? "12"
                                        let year = JSON["year"] as? String ?? "2021"
                                        
                                        let ageRequired = month + "/" + day + "/" + year
                                        
                                        let calculatedAge = getAge.calcAge(birthday: ageRequired)
                                        
                                        if(Int(calculatedAge) > 17){
                                            relocateLocation()
                                            changeFunction()
                                            connected = "true"
                                            
                                            mapType = "get_location_public"
                                            privacy = "no"
                                            
                                            connectLocation()
                                        }else {
                                            self    .showToast("You do not meet the age required")
                                            changeFunction()
                                        }
                                        
                                    }else{
                                        changeFunction()
                                    }
                                case let .failure(error):
                                    self.showToast("Error getting eligibility status")
                                    changeFunction()
                                }
                            }
                        }))
                        
                        alert.addAction(UIAlertAction(title: "Friends", style: .default , handler:{ [self] (UIAlertAction)in
                            relocateLocation()
                            changeFunction()
                            connected = "true"
                            privacy = "no"
                            mapType = "get_location_private"
                            
                            connectLocation()
                        }))
                        
                        self.present(alert, animated: true, completion: {
                            alert.view.superview?.isUserInteractionEnabled = true
                            alert.view.superview?.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(self.alertControllerBackgroundTapped)))
                        })
                        
                    }else {
                        changeFunction()
                        mapView.clear()
                        deleteLocation()
                    }
                    break
                    
                case .restricted, .denied:
                    // Disable location features
                    PermissionAlert.MapPermission(navigationController: navigationController!)
                    
                    break
                    
                case .authorizedWhenInUse, .authorizedAlways:
                    // Enable features that require location services here.
                    
                    if(connected == "false")
                    {
                        let alert = UIAlertController(title: "Connect", message: "You and other users who are currently connected will be shown on the map.", preferredStyle: .alert)
                        
                        alert.addAction(UIAlertAction(title: "Global", style: .default , handler:{ [self] (UIAlertAction)in
                            
                            let parameters: Parameters=[
                                "type": "get",
                                "username": currentUser,
                                
                            ]
                            
                            AF.request(URL_Age, method: .post, parameters: parameters).responseString
                            { [self]
                                response in
                                switch response.result {
                                case let .success(value):
                                    if let JSON = value as? [String: Any]
                                    {
                                        let day = JSON["day"] as? String ?? "23"
                                        let month = JSON["month"] as? String ?? "12"
                                        let year = JSON["year"] as? String ?? "2021"
                                        
                                        let ageRequired = month + "/" + day + "/" + year
                                        
                                        let calculatedAge = getAge.calcAge(birthday: ageRequired)
                                        
                                        if(Int(calculatedAge) > 17){
                                            relocateLocation()
                                            changeFunction()
                                            connected = "true"
                                            mapType = "get_location_public"
                                            privacy = "no"
                                            connectLocation()
                                        }else {
                                            self.showToast("You do not meet the age required")
                                            changeFunction()
                                        }
                                        
                                    }else{
                                        changeFunction()
                                    }
                                case let .failure(error):
                                    self.showToast("Error getting eligibility status")
                                    changeFunction()
                                    print("XAS","Error")
                                }
                            }
                        }))
                        
                        alert.addAction(UIAlertAction(title: "Friends", style: .default , handler:{ [self] (UIAlertAction)in
                            changeFunction()
                            relocateLocation()
                            connected = "true"
                            privacy = "no"
                            mapType = "get_location_private"
                            connectLocation()
                        }))
                        
                        self.present(alert, animated: true, completion: {
                            alert.view.superview?.isUserInteractionEnabled = true
                            alert.view.superview?.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(self.alertControllerBackgroundTapped)))
                        })
                        
                    }else {
                        changeFunction()
                        mapView.clear()
                        deleteLocation()
                    }
                    
                    break
                }
            }
        }
    }
    
    func changeFunction()
    {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { [self] in // Change
            func1 = "no"
            if(ConnectLabel.text == "connect".localized()){
                ConnectLabel.text = "leave".localized()
            }else {
                ConnectLabel.text = "connect".localized()
            }
        }
    }
    
    @objc func alertControllerBackgroundTapped()
    {
        func1 = "no"
        self.dismiss(animated: true, completion: nil)
    }
    
    func drawImageWithProfilePic(pp: UIImage) -> UIImage {
        
        //            let imgView = UIImageView(image: image)
        //             imgView.frame = CGRect(x: 0, y: 0, width: 100, height: 100)
        
        let picImgView = UIImageView(image: pp)
        picImgView.frame = CGRect(x: 0, y: 0, width: 40, height: 40)
        
        
        picImgView.center.x = view.center.x
        picImgView.center.y = view.center.y - 7
        picImgView.layer.cornerRadius = picImgView.frame.width/2
        picImgView.clipsToBounds = true
        picImgView.setNeedsLayout()
        
        let newImage = imageWithView(view: picImgView)
        return newImage
    }
    
    func imageWithView(view: UIView) -> UIImage {
        var image: UIImage?
        UIGraphicsBeginImageContextWithOptions(view.bounds.size, false, 0.0)
        if let context = UIGraphicsGetCurrentContext() {
            view.layer.render(in: context)
            image = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
        }
        return image ?? UIImage()
    }
    
    func configLocation(Type: String, Username: String, Longitude: Double, Latitude: Double)
    {
        let url_connect1 = url_connect.url + "location.php"
        
        let parameters =  [
            "type": Type,
            "username": Username,
            "longitude": String(Longitude),
            "latitude": String(Latitude),
            "privacy": privacy
        ]
        
        AF.request(url_connect1, method: .post, parameters: parameters, encoding: URLEncoding.default, headers: nil).responseString {
            response in
            switch response.result {
            case let .success(value):
                
                print("hurdur")
            case let .failure(error):
                print("Error")
                print(error)
            }
        }
    }
    
    func connectLocation()
    {
        let url_connect1 = url_connect.url + "location.php"
        
        let parameters =  [
            "type": mapType,
            "username": currentUser,
        ]
        
        AF.request(url_connect1, method: .post, parameters: parameters, encoding: URLEncoding.default, headers: nil).responseJSON { [self]
            response in
            switch response.result {
            case let .success(value):
                
                if let objJson = value as? NSArray
                {
                    for element in objJson {
                        let data = element as! NSDictionary
                        
                        let username = data["username"] as! String
                        let privacyUser = data["privacy"] as! String
                        
                        if(privacyUser == "no"){
                            if(username != currentUser){
                                connected = "true"
                                
                                let latitude = data["latitude"] as? String ?? "0"
                                let longitude = data["longitude"] as? String ?? "0"
                                
                                let url_image = url_connect.url + "images/"
                                
                                let url = URL(string: url_image + username + ".png")
                                let urlData = try? Data(contentsOf: url!)
                                
                                let marker = GMSMarker()
                                marker.position = CLLocationCoordinate2D(latitude: Double(latitude)!, longitude: Double(longitude)!)
                                
                                marker.snippet = username
                                
                                
                                if(urlData != nil){
                                    marker.icon = self.drawImageWithProfilePic(pp: UIImage.init(data:urlData!) ?? UIImage(named: "ProfileIcon")!)
                                }else {
                                    marker.icon = self.drawImageWithProfilePic(pp: UIImage(named: "ProfileIcon")!)
                                }
                                
                                marker.appearAnimation = GMSMarkerAnimation.pop
                                marker.map = self.mapView
                                
                                marker.map = mapView
                                
                            }
                        }
                    }
                }else {
                    print("XAS", value)
                }
            case let .failure(error):
                print("XAS",error)
            }
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        deleteLocation()
    }
    
    func deleteLocation()
    {
        let url_connect1 = url_connect.url + "location.php"
        
        let parameters =  [
            "type": "delete_location",
            "username": currentUser,
            "longitude": "",
            "latitude": "",
            "privacy": ""
        ]
        
        AF.request(url_connect1, method: .post, parameters: parameters, encoding: URLEncoding.default, headers: nil).responseString { [self]
            response in
            switch response.result {
            case let .success(value):
                privacy = "no"
                connected = "false"
            case let .failure(error):
                print("error")
            }
        }
    }
}


// MARK: - CLLocationManagerDelegate
//1
extension Home: CLLocationManagerDelegate, GMSMapViewDelegate {
    // 2
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        // 3
        guard status == .authorizedWhenInUse else {
            
            return
        }
        
        locationManager.startUpdatingLocation()
        
        //5
        mapView.isMyLocationEnabled = true
        mapView.settings.myLocationButton = true
        
    }
    
    // 6
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        guard let location = locations.first else {
            return
        }
        
        
        let url_image = url_connect.url + "images/"
        let configName = UserDefaults.standard.getUsername()
        
        let url = URL(string: url_image + configName + ".png")
        let data = try? Data(contentsOf: url!)
        
        
        let marker = GMSMarker()
        marker.position = CLLocationCoordinate2D(latitude: locationManager.location?.coordinate.latitude ?? 0.0, longitude: locationManager.location?.coordinate.longitude ?? 0.0)
        
        marker.snippet = currentUser
        
        //    GMSMarker.addObserver(self, forKeyPath: , options: <#T##NSKeyValueObservingOptions#>, context: <#T##UnsafeMutableRawPointer?#>)
        
        if(data != nil){
            marker.icon = self.drawImageWithProfilePic(pp: UIImage.init(data:data!) ?? UIImage(named: "ProfileIcon")!)
        }else {
            marker.icon = self.drawImageWithProfilePic(pp: UIImage(named: "ProfileIcon")!)
        }
        
        zoomIn.isHidden = false
        zoomOut.isHidden = false
        
        marker.appearAnimation = GMSMarkerAnimation.pop
        marker.map = mapView
        
        configLocation(Type: "set_location", Username: currentUser, Longitude: locationManager.location?.coordinate.longitude ?? 0.0, Latitude: locationManager.location?.coordinate.latitude ?? 0.0)
        
        // 7
        mapView.camera = GMSCameraPosition(target: location.coordinate, zoom: 15, bearing: 0, viewingAngle: 0)
        
        // 8
        locationManager.stopUpdatingLocation()
        
        if(connected == "true"){
            changeLocationType()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print(error)
    }
    
    func mapView(_ mapView: GMSMapView, didChange position: GMSCameraPosition) {
        zoom = mapView.camera.zoom
    }
    
    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        
        if(currentUser != marker.snippet!){
            let vc = MapViewSheet()
            vc.modalPresentationStyle = .overCurrentContext
            vc.NameLabel.text = marker.snippet!
            vc.delegate = self
            // Keep animated value as false
            // Custom Modal presentation animation will be handled in VC itself
            self.navigationController!.present(vc, animated: false)
        }
        
        return false // or false as needed.
    }
}

extension Home: HomeDelegate{
    func message(username: String, profilePic: String, unread: String) {
        goToMessages(username: username, profileImage: profilePic, unreadCount: unread)
    }
    
    func profile(username: String, profilePic: String) {
        goToProfile(username: username, profileImage: profilePic)
    }
    
    
}

