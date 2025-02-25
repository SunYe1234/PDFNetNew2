// This file is autogenerated: please see the codegen template "Options"
namespace pdftron{ namespace PDF{ 

OfficeToPDFOptions::OfficeToPDFOptions()
	: ConversionOptions()
{
}

OfficeToPDFOptions::~OfficeToPDFOptions()
{
}


UString OfficeToPDFOptions::GetLayoutResourcesPluginPath()
{
	SDF::Obj found = m_dict.FindObj("LayoutResourcesPluginPath");
	if(!found.IsNull())
	{
		return found.GetAsPDFText();
	}
	return "";
}

OfficeToPDFOptions& OfficeToPDFOptions::SetLayoutResourcesPluginPath(UString value)
{
	m_dict.PutText("LayoutResourcesPluginPath", value);
	return *this;
}


UString OfficeToPDFOptions::GetResourceDocPath()
{
	SDF::Obj found = m_dict.FindObj("ResourceDocPath");
	if(!found.IsNull())
	{
		return found.GetAsPDFText();
	}
	return "";
}

OfficeToPDFOptions& OfficeToPDFOptions::SetResourceDocPath(UString value)
{
	m_dict.PutText("ResourceDocPath", value);
	return *this;
}


UString OfficeToPDFOptions::GetSmartSubstitutionPluginPath()
{
	SDF::Obj found = m_dict.FindObj("SmartSubstitutionPluginPath");
	if(!found.IsNull())
	{
		return found.GetAsPDFText();
	}
	return "";
}

OfficeToPDFOptions& OfficeToPDFOptions::SetSmartSubstitutionPluginPath(UString value)
{
	m_dict.PutText("SmartSubstitutionPluginPath", value);
	return *this;
}

double OfficeToPDFOptions::GetExcelDefaultCellBorderWidth()
{
	SDF::Obj found = m_dict.FindObj("ExcelDefaultCellBorderWidth");
	if(!found.IsNull())
	{
		return found.GetNumber();
	}
	return 0.0;
}

OfficeToPDFOptions& OfficeToPDFOptions::SetExcelDefaultCellBorderWidth(double value)
{
	m_dict.PutNumber("ExcelDefaultCellBorderWidth", value);
	return *this;
}



}
}
